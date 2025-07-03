package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.ScoredPost;
import pl.jakubkonkol.tasteitserver.service.interfaces.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PostRankingService implements IPostRankingService {
    private final IPostCandidatesService postCollectionService;
    private final IFeedScoringService scoringService;
    private final IContentFilterService contentFilterService;
    private static final Logger LOGGER = Logger.getLogger(PostRankingService.class.getName());

    @Cacheable(value = "rankedPosts", key = "#userId")

    public List<Post> getRankedPostsForUser(User currentUser, String userId) {
     
        List<Post> candidates = postCollectionService.collectPosts(currentUser);
        List<Post> filteredCandidates = contentFilterService.filterBannedContent(candidates, currentUser);
        LOGGER.log(Level.INFO, "Filtered out {0} posts with banned content",
                candidates.size() - filteredCandidates.size());
        List<ScoredPost> scoredPosts = scoringService.calculateScores(filteredCandidates, currentUser);

        return scoredPosts.stream()
                .map(ScoredPost::post)
                .toList();
    }

    @CacheEvict(value = "rankedPosts", key = "#userId")
    public void clearRankedPostsCacheForUser(String userId) {
        LOGGER.log(Level.INFO, "Cleared ranked posts cache for user {0}", userId);
    }
}
