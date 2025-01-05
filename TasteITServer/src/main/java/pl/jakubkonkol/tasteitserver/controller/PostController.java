package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.CommentDto;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.service.interfaces.ICommentService;
import pl.jakubkonkol.tasteitserver.service.interfaces.ILikeService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
    private final IPostService postService;
    private final ILikeService likeService;
    private final ICommentService commentService;

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable String postId, @RequestHeader("Authorization") String sessionToken) {
        PostDto postDto = postService.getPost(postId, sessionToken);
/*        if (postDto == null) {
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }*/
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/feed")
    public ResponseEntity<PageDto<PostDto>> getRandomPosts(@RequestParam(defaultValue = "0") Integer page,
                                                        @RequestParam(defaultValue = "20") Integer size,
                                                           @RequestHeader("Authorization") String sessionToken) {
        PageDto<PostDto> pageDto = postService.getRandomPosts(page, size, sessionToken);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/{postId}/recipe")
    public ResponseEntity<Recipe> getPostRecipe(@PathVariable String postId) {
        Recipe recipe = postService.getPostRecipe(postId);
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<GenericResponse> likePost(@PathVariable String postId, @RequestHeader("Authorization") final String sessionToken) {
        likeService.likePost(postId, sessionToken);

        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Post Liked")
                .build());
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<GenericResponse> unlikePost(@PathVariable String postId, @RequestHeader("Authorization") final String sessionToken) {
        likeService.unlikePost(postId, sessionToken);

        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Post Unliked")
                .build());
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable String postId, @RequestBody CommentDto commentDto, @RequestHeader("Authorization") final String sessionToken) {
        CommentDto dto = commentService.addComment(postId, commentDto, sessionToken);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<GenericResponse> deleteComment(@PathVariable String postId, @PathVariable String commentId, @RequestHeader("Authorization") final String sessionToken) {
        commentService.deleteComment(postId, commentId, sessionToken);

        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Comment Deleted")
                .build());
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable String postId) {
        List<CommentDto> commentDtos = commentService.getComments(postId);
        return ResponseEntity.ok(commentDtos);
    }

    @GetMapping("/likedby/{userId}")
    public ResponseEntity<List<PostDto>> getPostsLikedByUser(@PathVariable String userId, @RequestHeader("Authorization") final String sessionToken) {

        var posts = postService.getPostsLikedByUser(userId, sessionToken);

        return ResponseEntity.ok(posts);
    }

    @PostMapping("/create")
    public ResponseEntity<PostDto> createPost(@RequestBody @Valid PostDto postDto, @RequestHeader("Authorization") final String sessionToken) {
        PostDto post = postService.createPost(postDto, sessionToken);
        return ResponseEntity.ok(post);
    }
}
