package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.CommentDto;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Comment;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.repository.CommentRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public CommentDto addComment(String postId, CommentDto commentDto, String token) {
        UserReturnDto userByToken = userService.getCurrentUserDtoBySessionToken(token);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (commentDto.getContent() == null || commentDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty.");
        }

        Comment comment = Comment.builder()
                .postId(post.getPostId())
                .userId(userByToken.getUserId())
                .content(commentDto.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.getComments().add(savedComment);
        postRepository.save(post);

        return convertToDto(savedComment);
    }

    public void deleteComment(String postId, String commentId, String token) {
        UserReturnDto userByToken = userService.getCurrentUserDtoBySessionToken(token);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUserId().equals(userByToken.getUserId())) {
            throw new IllegalStateException("User can only delete his own comments");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.getComments().remove(comment);
        postRepository.save(post);
        commentRepository.delete(comment);
    }

    public List<CommentDto> getComments(String postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(this::convertToDto)
                .toList();
    }

    private CommentDto convertToDto(Comment comment) {
        return modelMapper.map(comment, CommentDto.class);
    }
}
