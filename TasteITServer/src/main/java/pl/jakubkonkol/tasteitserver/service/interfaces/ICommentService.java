package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.CommentDto;

import java.util.List;

public interface ICommentService {
    CommentDto addComment(String postId, CommentDto commentDto, String token);
    void deleteComment(String postId, String commentId, String token);
    List<CommentDto> getComments(String postId);
    void deleteAll();
}
