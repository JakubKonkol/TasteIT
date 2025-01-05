package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.ScoredPost;
import pl.jakubkonkol.tasteitserver.service.interfaces.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
@RequiredArgsConstructor
public class RankerService implements IRankerService {
    private final IPostCandidatesService postCollectionService;
    private final IFeedScoringService scoringService;
    private final IUserService userService;
    private final IPostService postService;
    private final IContentFilterService contentFilterService;
    private static final Logger LOGGER = Logger.getLogger(RankerService.class.getName());

    @Override
    public PageDto<PostDto> rankPosts(Integer page, Integer size, String sessionToken) {

        // 1. Get current user
        User currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        // 2. Collect candidate posts
        List<Post> candidates = postCollectionService.collectPosts(currentUser);

        List<Post> filteredCandidates = contentFilterService.filterBannedContent(candidates, currentUser);
        LOGGER.log(Level.INFO, "Filtered out {0} posts with banned content",
                candidates.size() - filteredCandidates.size());

        // 3. Calculate scores and sort
        List<ScoredPost> scoredPosts = scoringService.calculateScores(filteredCandidates, currentUser);

        // 4. Prepare a response with pagination
        List<Post> rankedPosts = scoredPosts.stream()
                .map(ScoredPost::post)
                .toList();

        return postService.convertPostsToPageDto(
                sessionToken,
                rankedPosts,
                PageRequest.of(page, size)
        );
    }
}