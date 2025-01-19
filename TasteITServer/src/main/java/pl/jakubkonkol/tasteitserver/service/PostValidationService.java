package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.exception.UnauthorizedException;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostValidationService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostValidationService implements IPostValidationService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public void deletePost(String postId, String sessionToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!post.getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("User is not authorized to delete this post");
        }

        postRepository.deleteById(postId);
    }
}
