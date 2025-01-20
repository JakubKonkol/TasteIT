package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final IPostService postService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserReturnDto> getUserById(@PathVariable String userId, @RequestHeader("Authorization") String sessionToken) {
        var user = userService.getUserDtoById(userId, sessionToken);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserReturnDto> getUserProfileById(@PathVariable String userId, @RequestHeader("Authorization") String sessionToken) {
        var user = userService.getUserProfileView(userId, sessionToken);
        return ResponseEntity.ok(user);
    }

    @GetMapping()
    public ResponseEntity<UserReturnDto> getCurrentUserBySessionToken(@RequestHeader("Authorization") String sessionToken) {
        var user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-user-profile")
    public ResponseEntity<GenericResponse> updateUserProfile(@Valid @RequestBody UserProfileDto userProfileDto, @RequestHeader("Authorization") String sessionToken) {
        userService.updateUserProfile(userProfileDto, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("User updated")
                .build());
    }

    @PatchMapping("/first-login/{userId}")
    public ResponseEntity<GenericResponse> changeUserFirstLogin(@PathVariable String userId, @RequestHeader("Authorization") String sessionToken) {
        userService.changeUserFirstLogin(userId, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("User updated")
                .build());
    }

    @PatchMapping("/tags/{userId}")
    public ResponseEntity<GenericResponse> updateUserTags(@PathVariable String userId, @RequestBody UserTagsDto userTagsDto, @RequestHeader("Authorization") String sessionToken) {
        userService.updateUserTags(userId, userTagsDto, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("User updated")
                .build());
    }

    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<GenericResponse> followUser(@PathVariable String targetUserId, @RequestHeader("Authorization") String sessionToken) {
        userService.followUser(targetUserId, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Followed")
                .build());
    }

    @DeleteMapping("/unfollow/{targetUserId}")
    public ResponseEntity<GenericResponse> unfollowUser(@PathVariable String targetUserId, @RequestHeader("Authorization") String sessionToken) {
        userService.unfollowUser(targetUserId, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Unfollowed")
                .build());
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<PageDto<UserReturnDto>> getFollowers(
            @PathVariable String userId,
            @RequestHeader("Authorization") String sessionToken,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageDto<UserReturnDto> followers = userService.getFollowers(userId, sessionToken, page, size);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<PageDto<UserReturnDto>> getFollowing(
            @PathVariable String userId,
            @RequestHeader("Authorization") String sessionToken,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageDto<UserReturnDto> following = userService.getFollowing(userId, sessionToken, page, size);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<PageDto<PostDto>> getUserPosts(@PathVariable String userId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        PageDto<PostDto> posts = postService.getUserPosts(userId, page, size);
        return ResponseEntity.ok(posts);
    }

    @PatchMapping("/banned-ingredients")
    public ResponseEntity<GenericResponse> updateUserBannedIngredients(@RequestHeader("Authorization") String sessionToken, @RequestBody List<IngredientDto> ingredients) {
        userService.updateUserBannedIngredients(sessionToken, ingredients);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Banned Ingredients updated")
                .build());
    }

    @PatchMapping("/banned-tags")
    public ResponseEntity<GenericResponse> updateUserBannedTags(@RequestHeader("Authorization") String sessionToken, @RequestBody List<TagDto> tags) {
        userService.updateUserBannedTags(sessionToken, tags);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Banned Tags updated")
                .build());
    }
}
