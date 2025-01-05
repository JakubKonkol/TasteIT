package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.Cluster;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.enums.ClusterStatus;
import pl.jakubkonkol.tasteitserver.repository.ClusterRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IClusteringService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClusteringService implements IClusteringService {
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ClusterRepository clusterRepository;
    private static final Logger LOGGER = Logger.getLogger(ClusteringService.class.getName());

    private final static String CLUSTERING_GROUP =  "clustering-group";
    private final static String CLUSTERING_TOPIC = "clustering-response";
    private final static String CLUSTERING_REQUEST_TOPIC = "clustering-request";

    @Value("${app.clustering.legacy-cutoff-days:30}")
    private int legacyCutoffDays;

    @Value("${app.clustering.min-usage-count:1}")
    private int minUsageCount;

    @Value("${app.clustering.batch-size:100}")
    private int clusteringBatchSize;

    /**
     * Scheduled job to update clusters regularly
     * Runs at 1 AM every day by default
     */
    @Scheduled(cron = "${app.clustering.schedule:0 0 1 * * *}")
    public void scheduleClusteringUpdate() {
        LOGGER.log(Level.INFO, "Starting scheduled clustering update");
        long startTime = System.currentTimeMillis();

        try {
            markExistingClustersAsLegacy();
            requestClustering();
            cleanupUnusedClusters();

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.INFO, "Completed clustering update in {0}ms", duration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during scheduled clustering update", e);
        }
    }

    private void markExistingClustersAsLegacy() {
        long startTime = System.currentTimeMillis();
        List<Cluster> activeClusters = clusterRepository.findByStatus(ClusterStatus.ACTIVE);
        activeClusters.forEach(cluster -> {
            cluster.setStatus(ClusterStatus.LEGACY);
        });
        clusterRepository.saveAll(activeClusters);

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.INFO, "Marked {0} clusters as legacy in {1}ms",
                new Object[]{activeClusters.size(), duration});
    }

    private void cleanupUnusedClusters() {
        long startTime = System.currentTimeMillis();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(legacyCutoffDays);

        // Find unused legacy clusters
        List<Cluster> unusedClusters = clusterRepository.findUnusedLegacyClusters(cutoffDate);
        List<Cluster> lowUsageClusters = clusterRepository.findLowUsageClusters(minUsageCount);

        // Combine lists and remove duplicates
        Set<Cluster> clustersToRemove = new HashSet<>();
        clustersToRemove.addAll(unusedClusters);
        clustersToRemove.addAll(lowUsageClusters);

        if (!clustersToRemove.isEmpty()) {
            clusterRepository.deleteAll(clustersToRemove);

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.INFO,
                    "Removed {0} unused clusters in {1}ms",
                    new Object[]{clustersToRemove.size(), duration});
        }
    }

    /**
     * Initiates clustering process by sending data to Python service
     */
    public void requestClustering() {
        String correlationId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        long totalPosts = postRepository.count();

        LOGGER.log(Level.INFO,
                "Starting clustering request for {0} posts with batch size {1}",
                new Object[]{totalPosts, clusteringBatchSize});

        for (int offset = 0; offset < totalPosts; offset += clusteringBatchSize) {
            List<Post> postsBatch = postRepository.findAll(
                    PageRequest.of(offset / clusteringBatchSize, clusteringBatchSize)
            ).getContent();

            List<PostDto> postDtos = postsBatch.stream()
                    .map(post -> modelMapper.map(post, PostDto.class))
                    .toList();

            Map<String, Object> payload = new HashMap<>();
            payload.put("posts", postDtos);
            payload.put("correlationId", correlationId);
            payload.put("batchNumber", offset / clusteringBatchSize);
            payload.put("totalBatches", (totalPosts + clusteringBatchSize - 1) / clusteringBatchSize);

            kafkaTemplate.send(CLUSTERING_REQUEST_TOPIC, correlationId, payload);

            LOGGER.log(Level.INFO, "Sent batch {0}/{1} of posts for clustering",
                    new Object[]{
                            offset / clusteringBatchSize + 1,
                            (totalPosts + clusteringBatchSize - 1) / clusteringBatchSize
                    });
        }

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.INFO, "Completed sending all clustering requests in {0}ms", duration);
    }

    /**
     * Handles clustering results from Python service
     */
    @Transactional
    @KafkaListener(
            topics = CLUSTERING_TOPIC,
            groupId = CLUSTERING_GROUP
    )
    public void handleClusteringResponse(@Payload Map<String, Object> response) {
        long startTime = System.currentTimeMillis();

        try {
            validateClusteringResponse(response);
            String correlationId = (String) response.get("correlationId");
            LOGGER.log(Level.INFO, "Processing response for correlationId: {0}", correlationId);

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> clustersData =
                    (Map<String, Map<String, Object>>) response.get("clusters");
            createClusters(clustersData);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> postsAssignments =
                    (List<Map<String, Object>>) response.get("posts_assignments");
            assignClusterToPosts(postsAssignments);

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.INFO,
                    "Successfully processed clustering response in {0}ms", duration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing clustering response", e);
            throw new RuntimeException("Error processing clustering response", e);
        }
    }

    /**
     * Validates the response format from clustering service
     */
    private void validateClusteringResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("correlationId")) {
            throw new IllegalArgumentException("Invalid response format - missing correlationId");
        }

        if (!response.containsKey("clusters") || !response.containsKey("posts_assignments")) {
            throw new IllegalArgumentException("Invalid response format - missing required data");
        }
    }

    /**
     * Creates or updates clusters based on Python service response
     */
    private void createClusters(Map<String, Map<String, Object>> clustersData) {
        long startTime = System.currentTimeMillis();

        try {
            // Get existing clusters for potential updates
            Map<String, Cluster> existingClusters = clusterRepository.findAll().stream()
                    .collect(Collectors.toMap(Cluster::getClusterId, cluster -> cluster));

            List<Cluster> clustersToSave = clustersData.entrySet().stream()
                    .map(entry -> createOrUpdateCluster(
                            entry.getValue(),
                            entry.getKey(),
                            existingClusters.get(entry.getKey())
                    ))
                    .toList();

            clusterRepository.saveAll(clustersToSave);
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.INFO,
                    "Saved {0} clusters in {1}ms",
                    new Object[]{clustersToSave.size(), duration});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating/updating clusters", e);
            throw new RuntimeException("Error creating/updating clusters", e);
        }
    }

    /**
     * Creates a new cluster or updates existing one while preserving usage statistics
     */
    private Cluster createOrUpdateCluster(Map<String, Object> clusterInfo, String clusterId, Cluster existingCluster) {
        Cluster newCluster = convertResponseToCluster(clusterInfo);
        newCluster.setClusterId(clusterId);
        newCluster.setStatus(ClusterStatus.ACTIVE);

        if (existingCluster != null) {
            // Preserve important metadata
            newCluster.setId(existingCluster.getId());
            newCluster.setUsageCount(existingCluster.getUsageCount());
            newCluster.setLastUsedDate(existingCluster.getLastUsedDate());
            newCluster.setCreatedDate(existingCluster.getCreatedDate());
        } else {
            newCluster.setCreatedDate(LocalDateTime.now());
        }

        return newCluster;
    }

    /**
     * Updates post-cluster assignments while preserving existing relevant assignments
     */
    // SIDE NOTE: If clusters and posts do not change often, we may consider caching them to reduce the number of queries
    private void assignClusterToPosts(List<Map<String, Object>> postsAssignments) {
        long startTime = System.currentTimeMillis();

        try {
            // Collect all relevant IDs
            Set<String> postIds = postsAssignments.stream()
                    .map(assignment -> assignment.get("post_id").toString())
                    .collect(Collectors.toSet());

            Set<String> newClusterIds = postsAssignments.stream()
                    .map(assignment -> assignment.get("cluster_id").toString())
                    .collect(Collectors.toSet());

            // Load all needed entities
            Map<String, Post> posts = postRepository.findAllById(postIds).stream()
                    .collect(Collectors.toMap(Post::getPostId, post -> post));

            Map<String, Cluster> availableClusters = clusterRepository.findByClusterIdIn(newClusterIds).stream()
                    .collect(Collectors.toMap(Cluster::getClusterId, cluster -> cluster));

            Set<Cluster> clustersToUpdate = new HashSet<>();

            // Process assignments
            postsAssignments.forEach(assignment ->
                    processPostAssignment(assignment, posts, availableClusters, clustersToUpdate));

            // Save updated posts
            postRepository.saveAll(posts.values());
            clusterRepository.saveAll(clustersToUpdate);
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.INFO,
                    "Updated {0} posts and {1} clusters in {2}ms",
                    new Object[]{posts.size(), clustersToUpdate.size(), duration});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error assigning clusters to posts", e);
            throw new RuntimeException("Error assigning clusters to posts", e);
        }
    }

    /**
     * Processes single post-cluster assignment while managing cluster usage statistics
     */
    private void processPostAssignment(
            Map<String, Object> assignment,
            Map<String, Post> posts,
            Map<String, Cluster> availableClusters,
            Set<Cluster> clustersToUpdate) {

        String postId = assignment.get("post_id").toString();
        String clusterId = assignment.get("cluster_id").toString();

        Post post = posts.get(postId);
        Cluster cluster = availableClusters.get(clusterId);

        if (post != null && cluster != null) {
            // Keep legacy clusters if they're still relevant
            Set<Cluster> legacyClusters = new HashSet<>();
            if (post.getClusters() != null) {
                legacyClusters = post.getClusters().stream()
                        .filter(Objects::nonNull)
                        .filter(c -> c.getStatus() == ClusterStatus.LEGACY)
                        .collect(Collectors.toSet());
            }

            // Start with new active cluster
            Set<Cluster> newClusters = new HashSet<>();
            newClusters.add(cluster);

            // Add still relevant legacy clusters
            newClusters.addAll(legacyClusters);

            // Update post's clusters
            post.setClusters(new ArrayList<>(newClusters));

            // Update cluster usage statistics
            cluster.setUsageCount(cluster.getUsageCount() + 1);
            cluster.setLastUsedDate(LocalDateTime.now());
            clustersToUpdate.add(cluster);
        }
    }

    /**
     * Converts clustering service response to Cluster entity
     */
    private Cluster convertResponseToCluster(Map<String, Object> clusterInfo) {
        Cluster cluster = new Cluster();

        cluster.setName((String) clusterInfo.get("name"));

        @SuppressWarnings("unchecked")
        List<String> mainTopics = (List<String>) clusterInfo.get("main_topics");
        cluster.setMainTopics(mainTopics);

        @SuppressWarnings("unchecked")
        Map<String, Double> keywordWeights = ((Map<String, Object>) clusterInfo.get("keyword_weights"))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Double.valueOf(e.getValue().toString())
                ));
        cluster.setKeywordWeights(keywordWeights);

        return cluster;
    }
}