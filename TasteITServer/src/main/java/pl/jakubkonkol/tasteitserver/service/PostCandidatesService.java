package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostCandidatesService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCandidatesService implements IPostCandidatesService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ExecutorService postFetchingExecutorService;

    private static final int TOTAL_POSTS_TARGET = 500;
    private static final int PREFERRED_POSTS_INITIAL = 350;  // Starting from 70%
    private static final int FOLLOWED_POSTS_INITIAL = 100;   // Starting from 20%
    private static final int RECENT_POSTS_LIMIT = 200;       // Limit for follow-up posts
    private static final int MIN_POSTS_PER_CLUSTER = 50;
    private static final Logger LOGGER = Logger.getLogger(PostCandidatesService.class.getName());

    public List<Post> collectPosts(User user) {
        Date cutoffDate = calculateCutoffDate(30);
        Set<String> collectedPostIds = new HashSet<>();
        List<Post> candidates = new ArrayList<>();

        // 1. Posts from preferred clusters (main source)
        List<Post> preferredPosts = getPreferredClusterPosts(user.getUserId(), cutoffDate);
        addUniquePostsToCollection(preferredPosts, candidates, collectedPostIds);

        // 2. Posts from users current user is following (additional source)
        if (!user.getFollowing().isEmpty()) {
            List<Post> followedPosts = getFollowedUsersPosts(user, cutoffDate, collectedPostIds);
            LOGGER.log(Level.INFO, "Found {0} posts from following users", followedPosts.size());
            addUniquePostsToCollection(followedPosts, candidates, collectedPostIds);
        }

        // 3. If we still don't have enough posts, we add the latest ones
        if (candidates.size() < TOTAL_POSTS_TARGET) {
            int remaining = TOTAL_POSTS_TARGET - candidates.size();
            List<Post> recentPosts = getRecentPosts(cutoffDate, collectedPostIds, remaining);
            LOGGER.log(Level.INFO, "Found {0} posts from last 30 days", recentPosts.size());
            addUniquePostsToCollection(recentPosts, candidates, collectedPostIds);
        }

        // 4. If we still don't have enough, we increase the time range
        if (candidates.size() < TOTAL_POSTS_TARGET) {
            Date extendedCutoffDate = calculateCutoffDate(90);
            int remaining = TOTAL_POSTS_TARGET - candidates.size();
            List<Post> olderPosts = getRecentPosts(extendedCutoffDate, collectedPostIds, remaining);
            LOGGER.log(Level.INFO, "Found {0} posts from last 90 days", olderPosts.size());
            addUniquePostsToCollection(olderPosts, candidates, collectedPostIds);
        }

        return candidates;
    }

    private synchronized void addUniquePostsToCollection(
            List<Post> newPosts,
            List<Post> candidates,
            Set<String> collectedIds) {
        int duplicatesCount = 0;
        for (Post post : newPosts) {
            if (collectedIds.add(post.getPostId())) {
                candidates.add(post);
            } else {
                duplicatesCount++;
            }
        }
        LOGGER.log(Level.INFO, "Skipped " + duplicatesCount + " duplicate posts out of" + newPosts.size() + " total posts");
    }

    private Date calculateCutoffDate(int days) {
        return Date.from(LocalDateTime.now()
                .minusDays(days)
                .toInstant(ZoneOffset.UTC));
    }

    private List<Post> getPreferredClusterPosts(String userId, Date cutoffDate) {
        Map<String, Double> preferences = userRepository.findClusterPreferencesById(userId)
                .map(User::getClusterPreferences)
                .orElse(new HashMap<>());

        if (preferences.isEmpty()) {
            return postRepository.findTop100ByOrderByCreatedDateDesc();
        }

        Map<String, Integer> postsPerCluster = calculatePostPerCluster(preferences);

        return getPostsFromEachCluster(cutoffDate, postsPerCluster);
    }

    private List<Post> getPostsFromEachCluster(Date cutoffDate, Map<String, Integer> postsPerCluster) {
        // Create thread-safe collections to store results
        List<Post> allPosts = Collections.synchronizedList(new ArrayList<>(PREFERRED_POSTS_INITIAL));
        Set<String> collectedPostIds = Collections.synchronizedSet(new HashSet<>(PREFERRED_POSTS_INITIAL));

        // Create tasks to download posts from each cluster in parallel
        List<CompletableFuture<Void>> fetchTasks = postsPerCluster.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() ->
                                fetchPostsFromClusterInBatches(
                                        entry.getKey(),         // cluster id
                                        entry.getValue(),       // Number of posts to download
                                        cutoffDate,
                                        allPosts,               // Shared list of all posts
                                        collectedPostIds        // Set of already collected IDs
                                ),
                        postFetchingExecutorService))
                .toList();

        // Waiting for all tasks to be completed
        try {
            CompletableFuture.allOf(fetchTasks.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while fetching cluster posts: " + e.getMessage());
        }

        return allPosts;
    }

    private void fetchPostsFromClusterInBatches(
            String clusterId,
            int totalToFetch,
            Date cutoffDate,
            List<Post> allPosts,
            Set<String> collectedPostIds) {

        try {
            long startTime = System.currentTimeMillis();
            int batchSize = 100;        // Optimal batch size
            int fetchedSoFar = 0;       // Collected posts counter

            LOGGER.log(Level.INFO, "Starting fetch for cluster {0} with limit {1}",
                    new Object[]{clusterId, totalToFetch});

            // Get posts in a loop until we reach the limit or run out of posts
            while (fetchedSoFar < totalToFetch) {
                // Calculate the size of the next batch
                int currentBatchSize = Math.min(batchSize, totalToFetch - fetchedSoFar);

                // Collect a batch of posts
                List<Post> batchPosts = postRepository.findByClustersAndCreatedDateAfter(
                        new ObjectId(clusterId),
                        cutoffDate,
                        PageRequest.of(fetchedSoFar / batchSize, currentBatchSize)
                );

                if (batchPosts.isEmpty()) break;

                // Safely adding posts to the main collection
                synchronized (allPosts) {
                    addUniquePostsToCollection(batchPosts, allPosts, collectedPostIds);
                }

                fetchedSoFar += batchPosts.size();

                if (batchPosts.size() < currentBatchSize) break; // No more posts
            }

            logPerformanceMetrics("Fetch posts from cluster " + clusterId, startTime);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching posts from cluster {0}: {1}",
                    new Object[]{clusterId, e.getMessage()});
        }
    }

    private Map<String, Integer> calculatePostPerCluster(Map<String, Double> preferences) {
        Map<String, Long> availablePosts = getAvailablePostsCount(preferences.keySet());
        Map<String, Integer> postsPerCluster = calculateInitialAllocation(preferences, availablePosts);

        redistributeRemainingPosts(postsPerCluster, preferences, availablePosts);

        logFinalAllocation(postsPerCluster, availablePosts);

        return postsPerCluster;
    }

    /**
     * Checks how many posts are available in each cluster
     */
    private Map<String, Long> getAvailablePostsCount(Set<String> clusterIds) {
        // 1. Set a cut-off date for posts (30 days back)
        Date cutoffDate = calculateCutoffDate(30);

        // 2. Create a list of tasks to be executed in parallel - a separate task for each cluster
        List<CompletableFuture<Map.Entry<String, Long>>> futures = clusterIds.stream()
                .map(clusterId -> CompletableFuture.supplyAsync(() -> {
                    // Measure the execution time for each cluster
                    long startTime = System.currentTimeMillis();
                    // Count posts in a given cluster
                    long count = postRepository.countByClustersAndCreatedDateAfter(
                            new ObjectId(clusterId),
                            cutoffDate
                    );

                    logPerformanceMetrics("Count posts in cluster " + clusterId, startTime);

                    // Return a pair: cluster ID -> number of posts
                    return Map.entry(clusterId, count);
                }, postFetchingExecutorService))
                .toList();

        // Wait for all results and combine them into a map
        try {
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)       // Get the results from each task
                            .peek(entry -> LOGGER.log(Level.INFO,
                                    "Cluster {0} has {1} available posts",
                                    new Object[]{entry.getKey(), entry.getValue()}))
                            .collect(Collectors.toMap(          // Build a map of results
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (v1, v2) -> v1,             // In case of duplicates we take the first value
                                    HashMap::new
                            )))
                    .get(30, TimeUnit.SECONDS);         // Waiting max 30 seconds
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while fetching cluster posts counts: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, Integer> calculateInitialAllocation(
            Map<String, Double> preferences,
            Map<String, Long> availablePosts
    ) {
        double totalWeight = calculateTotalWeight(preferences, availablePosts);
        Map<String, Integer> postsPerCluster = new HashMap<>();

        preferences.forEach((clusterId, weight) -> {
            long available = availablePosts.get(clusterId);
            if(available > 0) {
                int allocation = calculateClusterAllocation(weight, totalWeight, available);
                postsPerCluster.put(clusterId, allocation);
                logInitialAllocation(clusterId, allocation);
            } else {
                postsPerCluster.put(clusterId, 0);
            }
        });

        return postsPerCluster;
    }

    /**
     * Calculates the sum of weights for clusters that have posts available
     */
    private double calculateTotalWeight(
            Map<String, Double> preferences,
            Map<String, Long> availablePosts) {
        return preferences.entrySet().stream()
                .filter(entry -> availablePosts.get(entry.getKey()) > 0)
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    /**
     * Calculates the post allocation for a single cluster
     */
    private int calculateClusterAllocation(double weight, double totalWeight, long available) {
        double normalizedWeight = weight / totalWeight;
        int initial = (int) (PREFERRED_POSTS_INITIAL * normalizedWeight);
        initial = Math.max(initial, MIN_POSTS_PER_CLUSTER);
        return Math.min(initial, (int) available);
    }

    private void redistributeRemainingPosts(
            Map<String, Integer> postsPerCluster,
            Map<String, Double> preferences,
            Map<String, Long> availablePosts) {

        int remaining = calculateRemainingPosts(postsPerCluster);
        if (remaining <= 0) return;

        LOGGER.log(Level.INFO, "Distributes the remaining {0} posts", remaining);
        List<String> sortedClusterIds = getSortedClustersByWeight(preferences);

        for (String clusterId : sortedClusterIds) {
            if (remaining <= 0) break;
            remaining = addRemainingToCluster(
                    clusterId,
                    remaining,
                    postsPerCluster,
                    availablePosts
            );
        }
    }

    private int calculateRemainingPosts(Map<String, Integer> postsPerCluster) {
        int totalAssigned = postsPerCluster.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        return PREFERRED_POSTS_INITIAL - totalAssigned;
    }

    private List<String> getSortedClustersByWeight(Map<String, Double> preferences) {
        return preferences.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    private int addRemainingToCluster(
            String clusterId,
            int remaining,
            Map<String, Integer> postsPerCluster,
            Map<String, Long> availablePosts) {

        int current = postsPerCluster.get(clusterId);
        long available = availablePosts.get(clusterId);

        int canAdd = (int) Math.min(remaining, available - current);
        if (canAdd > 0) {
            postsPerCluster.put(clusterId, current + canAdd);
            LOGGER.log(Level.INFO, "Added {0} posts to cluster {1}",
                    new Object[]{canAdd, clusterId});
            return remaining - canAdd;
        }
        return remaining;
    }

    private void logInitialAllocation(String clusterId, int allocation) {
        LOGGER.log(Level.INFO, "Initial allocation for cluster {0}: {1} posts",
                new Object[]{clusterId, allocation});
    }

    private void logFinalAllocation(
            Map<String, Integer> postsPerCluster,
            Map<String, Long> availablePosts) {
        postsPerCluster.forEach((clusterId, count) ->
                LOGGER.log(Level.INFO, "Final allocation for cluster {0}: {1} posts (available: {2})",
                        new Object[]{clusterId, count, availablePosts.get(clusterId)}));
    }

    private void logPerformanceMetrics(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.INFO,
                "Performance: {0} completed in {1}ms",
                new Object[]{operation, duration}
        );
    }

    private List<Post> getFollowedUsersPosts(User user, Date cutoffDate, Set<String> excludePostIds) {
        return postRepository.findByUserIdInAndCreatedDateAfterAndPostIdNotIn(
                user.getFollowing(),
                cutoffDate,
                excludePostIds.stream().toList(),
                PageRequest.of(0, FOLLOWED_POSTS_INITIAL)
        );
    }

    private List<Post> getRecentPosts(Date cutoffDate, Set<String> excludePostIds, int limit) {
        return postRepository.findByCreatedDateAfterAndPostIdNotIn(
                cutoffDate,
                excludePostIds.stream().toList(),
                PageRequest.of(0, Math.min(limit, RECENT_POSTS_LIMIT))
        );
    }
}
