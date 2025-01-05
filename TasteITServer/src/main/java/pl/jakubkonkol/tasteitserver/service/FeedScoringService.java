package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.NormalizationValues;
import pl.jakubkonkol.tasteitserver.model.value.ScoredPost;
import pl.jakubkonkol.tasteitserver.service.interfaces.IFeedScoringService;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedScoringService implements IFeedScoringService {
    // Weights defining the influence of different components on the final result (sum = 1.0)
    public static final double BASE_SCORE_WEIGHT = 0.6;  // Basic Engagement Metrics
    public static final double TIME_BOOST_MAX = 0.3;     // Impact of post freshness
    public static final double SOCIAL_BOOST_MAX = 0.1;   // The influence of social factors

    // Weights for the components of the basic score (sum = 1.0)
    public static final double LIKE_WEIGHT = 0.4;
    public static final double COMMENT_WEIGHT = 0.6;

    // Time thresholds (in hours)
    public static final int FRESH_HOURS = 6;
    public static final int RECENT_HOURS = 24;

    public List<ScoredPost> calculateScores(List<Post> posts, User currentUser) {
        // Calculate values for normalization based on all posts
        NormalizationValues normValues = calculateNormalizationValues(posts);
        Date now = new Date();

        // CompletableFuture for parallel score calculation
        List<CompletableFuture<ScoredPost>> scoringTasks = posts.stream()
                .map(post -> CompletableFuture.supplyAsync(() ->
                        calculatePostScore(post, currentUser, now, normValues)))
                .toList();

        // Wait for all results and sort by score
        return scoringTasks.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(ScoredPost::score).reversed())
                .toList();
    }

    /**
     * Calculates the values used to normalize engagement metrics.
     * We use the 99th percentile instead of the maximum to avoid the influence of extreme values.
     * Example:
     * If we have posts with likes: [2, 3, 4, 1000] (where 1000 is the outlier),
     * then using the maximum (1000) would skew the normalization for all normal posts.
     */
    private NormalizationValues calculateNormalizationValues(List<Post> posts) {
        List<Integer> likesCount = posts.stream()
                .map(post -> post.getLikes().size())
                .collect(Collectors.toCollection(ArrayList::new));

        List<Integer> commentsCount = posts.stream()
                .map(post -> post.getComments().size())
                .collect(Collectors.toCollection(ArrayList::new));

        double maxLikes = calculatePercentile(likesCount);
        double maxComments = calculatePercentile(commentsCount);

        // Ensure that we will never divide by 0
        return new NormalizationValues(
                Math.max(maxLikes, 1.0),
                Math.max(maxComments, 1.0)
        );
    }

    private ScoredPost calculatePostScore(
            Post post,
            User currentUser,
            Date now,
            NormalizationValues normValues) {

        // 1. Calculate the basic engagement score (0-1)
        double baseScore = calculateBaseEngagementScore(post, normValues);

        // 2. Calculate freshness bonus (0-1)
        double timeBoost = calculateTimeBoost(post.getCreatedDate(), now);

        // 3. Calculate social bonus (0 lub 1)
        double socialBoost = calculateSocialBoost(post, currentUser);

        // Connect all components with their appropriate weights
        double finalScore = (baseScore * BASE_SCORE_WEIGHT) +
                (timeBoost * TIME_BOOST_MAX) +
                (socialBoost * SOCIAL_BOOST_MAX);

        return new ScoredPost(post, finalScore);
    }

    private double calculateBaseEngagementScore(Post post, NormalizationValues normValues) {
        // Normalize the number of likes and comments to 0-1 range
        double normalizedLikes = normalizeScore(post.getLikes().size(), normValues.maxLikes());
        double normalizedComments = normalizeScore(post.getComments().size(), normValues.maxComments());

        return (normalizedLikes * LIKE_WEIGHT) + (normalizedComments * COMMENT_WEIGHT);
    }

    private double calculateTimeBoost(Date postDate, Date now) {
        Duration age = Duration.between(postDate.toInstant(), now.toInstant());
        long hours = age.toHours();

        if (hours <= FRESH_HOURS) {
            return 1.0;  // Maximum boost for fresh posts
        } else if (hours <= RECENT_HOURS) {
            // Linear decrease between FRESH_HOURS and RECENT_HOURS
            return 1.0 - ((double)(hours - FRESH_HOURS) / (RECENT_HOURS - FRESH_HOURS));
        }
        return 0.0;  // No bonus for older posts
    }

    private double calculateSocialBoost(Post post, User currentUser) {
        if (currentUser.getFollowing().contains(post.getUserId())) {
            return 1.0;  // Full boost if user follows author
        }
        return 0.0;
    }

    /**
     * Calculates the 99th percentile from a list of values.
     * Example:
     * For a list [1,2,3,4,5,6,7,8,9,10], the 99th percentile is 9.9
     * For a list [1,1,1,2,2,2,100], the 99th percentile is approximately 2
     */
    private double calculatePercentile(List<Integer> values) {
        if (values.isEmpty()) return 1.0;

        Collections.sort(values);
        // Calculate the index for the 99th percentile
        int index = (int) Math.ceil(99 / 100.0 * values.size()) - 1;
        return Math.max(values.get(Math.max(index, 0)), 1.0);
    }

    /**
     * Normalizes value to 0-1 from max value
     */
    private double normalizeScore(double value, double maxValue) {
        if (maxValue <= 0) return 0;
        return Math.min(value / maxValue, 1.0);
    }
}
