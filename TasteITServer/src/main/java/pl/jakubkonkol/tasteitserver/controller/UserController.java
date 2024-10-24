package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.PostService;
import pl.jakubkonkol.tasteitserver.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserReturnDto> getUserById(@PathVariable String userId, @RequestHeader("Authorization") String sessionToken) {
        var user = userService.getUserDtoById(userId, sessionToken);
        return ResponseEntity.ok(user);
    }

    @GetMapping()
    public ResponseEntity<UserReturnDto> getCurrentUserBySessionToken(@RequestHeader("Authorization") String sessionToken) {
        var user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        return ResponseEntity.ok(user);
    }



    @PutMapping("/{userId}")
    public ResponseEntity<UserReturnDto> updateUserProfile(@PathVariable String userId, @RequestBody UserProfileDto userProfileDto) {
        var user = userService.updateUserProfile(userId, userProfileDto.getDisplayName(),
                userProfileDto.getBio(), userProfileDto.getProfilePicture(),
                userProfileDto.getBirthDate());
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/first-login/{userId}")
    public ResponseEntity<UserReturnDto> changeUserFirstLogin(@PathVariable String userId) {
        var user = userService.changeUserFirstLogin(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/tags/{userId}")
    public ResponseEntity<UserReturnDto> updateUserTags(@PathVariable String userId, @RequestBody UserTagsDto userTagsDto) {
        var user = userService.updateUserTags(userId, userTagsDto);
        return ResponseEntity.ok(user);
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
}
