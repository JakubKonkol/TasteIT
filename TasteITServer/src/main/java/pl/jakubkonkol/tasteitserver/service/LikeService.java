package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.annotation.RegisterAction;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;
import pl.jakubkonkol.tasteitserver.repository.LikeRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.ILikeService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostRankingService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class LikeService implements ILikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final NotificationEventPublisher notificationEventPublisher;
    private final IUserService userService;
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(LikeService.class.getName());
    private final IPostRankingService postRankingService;

    @RegisterAction(actionType = "LIKE_POST")
    @CacheEvict(value = "postById", key = "#postId")
    public void likePost(String postId, String token) {
        UserReturnDto userByToken = userService.getCurrentUserDtoBySessionToken(token);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        var existingLike = likeRepository.findByPostIdAndUserId(postId, userByToken.getUserId());
        if (existingLike.isPresent()) {
            throw new IllegalStateException("User has already liked this post.");
        }

        Like like = Like.builder()
                .postId(post.getPostId())
                .userId(userByToken.getUserId())
                .build();

        likeRepository.save(like);
        post.getLikes().add(like);
        postRepository.save(post);
        handleLikeNotification(post, userByToken.getUserId());
        postRankingService.clearRankedPostsCacheForUser(userByToken.getUserId());
    }

    @CacheEvict(value = "postById", key = "#postId")
    public void unlikePost(String postId, String token) {
        UserReturnDto userByToken = userService.getCurrentUserDtoBySessionToken(token);
        Like like = likeRepository.findByPostIdAndUserId(postId, userByToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.getLikes().remove(like);
        postRepository.save(post);
        likeRepository.delete(like);
        postRankingService.clearRankedPostsCacheForUser(userByToken.getUserId());
    }

    public void deleteAll() {
        List<Post> postsWithLikes = postRepository.findByLikesNotEmpty();

        if (!postsWithLikes.isEmpty()) {
            for (Post post : postsWithLikes) {
                post.getLikes().clear();
            }

            postRepository.saveAll(postsWithLikes);
            likeRepository.deleteAll();
        }
    }

    private void handleLikeNotification(Post post, String likerId) {
        if (!post.getUserId().equals(likerId)) {
            try {
                notificationEventPublisher.publishNotification(
                        NotificationType.POST_LIKE,
                        post.getUserId(),
                        likerId,
                        post.getPostId()
                );
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to send like notification", e);
            }
        }
    }
}
