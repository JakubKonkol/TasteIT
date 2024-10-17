package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.repository.LikeRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserService userService;

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
    }

    public void unlikePost(String postId, String token) {
        UserReturnDto userByToken = userService.getCurrentUserDtoBySessionToken(token);
        Like like = likeRepository.findByPostIdAndUserId(postId, userByToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.getLikes().remove(like);
        postRepository.save(post);
        likeRepository.delete(like);
    }

    public void deleteAll() {
        List<Post> postsWithLikes = postRepository.findByLikesNotEmpty();

        for (Post post : postsWithLikes) {
            post.getLikes().clear();
        }
        postRepository.saveAll(postsWithLikes);
        likeRepository.deleteAll();
    }
}
