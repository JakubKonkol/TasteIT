package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.service.interfaces.*;

import java.util.*;


@Service
@RequiredArgsConstructor
public class RankerService implements IRankerService {
    private final IUserService userService;
    private final IPostService postService;
    private final IPostRankingService postRankingService;

    public PageDto<PostDto> rankPosts(Integer page, Integer size, String sessionToken) {
        User currentUser = userService.getCurrentUserBySessionToken(sessionToken);
        List<Post> rankedPosts = postRankingService.getRankedPostsForUser(currentUser.getUserId());

        return postService.convertPostsToPageDto(
                sessionToken,
                rankedPosts,
                PageRequest.of(page, size)
        );
    }
}