package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.BadgeService;
import pl.jakubkonkol.tasteitserver.service.PostService;
import pl.jakubkonkol.tasteitserver.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final BadgeService badgeService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserReturnDto> getUserById(@PathVariable String userId, @RequestHeader("Authorization") String sessionToken) {
        var user = userService.getUserDtoById(userId, sessionToken);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile/{userId}") //temp endpoint
    public ResponseEntity<UserReturnDto> getUserProfileById(@PathVariable String userId, @RequestHeader("Authorization") String sessionToken) {
        var user = userService.getUserProfileView(userId, sessionToken);
        return ResponseEntity.ok(user);
    }

    @GetMapping()
    public ResponseEntity<UserReturnDto> getCurrentUserBySessionToken(@RequestHeader("Authorization") String sessionToken) {
        var user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        return ResponseEntity.ok(user);
    }

    @PutMapping()
    public ResponseEntity<GenericResponse> updateUserProfile(@Valid @RequestBody UserProfileDto userProfileDto) {
        userService.updateUserProfile(userProfileDto);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("User updated")
                .build());
    }

    @PatchMapping("/first-login/{userId}")
    public ResponseEntity<GenericResponse> changeUserFirstLogin(@PathVariable String userId) {
        userService.changeUserFirstLogin(userId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("User updated")
                .build());
    }

    @PatchMapping("/tags/{userId}")
    public ResponseEntity<GenericResponse> updateUserTags(@PathVariable String userId, @RequestBody UserTagsDto userTagsDto) {
        userService.updateUserTags(userId, userTagsDto);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("User updated")
                .build());
    }

    @PutMapping ("/{badgeId}/{userId}") //TODO moze trzeba to przerobic na put mapping i zapisywac caly biekt na nowe ale to trzoche bez sensu
    public ResponseEntity<String> grantBadgeToUser(@PathVariable String badgeId, @PathVariable String userId,
                                                   @RequestHeader("Authorization") String sessionToken){
        badgeService.grantBadgeToUser(badgeId, userId, sessionToken);
        return ResponseEntity.ok(userId + " " + badgeId);
    }


    //POST jest najbardziej naturalny w tym kontekście, ponieważ follow tworzy nową relację
    // (czyli nowe połączenie między dwoma użytkownikami), a unfollow usuwa tę relację.
    // POST semantycznie bardziej odpowiada operacjom związanym z modyfikacją lub tworzeniem czegoś nowego, np. relacji między użytkownikami.
    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<GenericResponse> followUser(@PathVariable String targetUserId, @RequestHeader("Authorization") String sessionToken) {
        userService.followUser(targetUserId, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Followed")
                .build());
    }
    //peracja unfollow usuwa istniejącą relację (z list following i followers),
    // co jest typową operacją usuwania zasobów, dlatego DELETE będzie bardziej naturalnym wyborem.
    // POST może działać, ale DELETE bardziej odpowiada konwencji REST, bo semantycznie usuwasz relację między dwoma zasobami (użytkownikami).
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
}

//POST
//users/id/badges/id