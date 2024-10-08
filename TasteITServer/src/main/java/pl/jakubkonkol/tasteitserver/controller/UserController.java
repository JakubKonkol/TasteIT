package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.service.PostService;
import pl.jakubkonkol.tasteitserver.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<UserReturnDto> getUserById(@PathVariable String userId) {

        var user = userService.getUserById(userId);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/getUserByToken")
    public ResponseEntity<UserReturnDto> getUserByToken(@RequestHeader("Authorization") String sessionToken) {

        var user = userService.getUserByToken(sessionToken);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/updateUserProfile/{userId}")
    public ResponseEntity<UserReturnDto> updateUserProfile(@PathVariable String userId, @RequestBody UserProfileDto userProfileDto) {

        var user = userService.updateUserProfile(userId, userProfileDto.getDisplayName(),
                userProfileDto.getBio(), userProfileDto.getProfilePicture(),
                userProfileDto.getBirthDate());

        return ResponseEntity.ok(user);
    }

    @PostMapping("/changeUserFirstLogin/{userId}")
    public ResponseEntity<UserReturnDto> changeUserFirstLogin(@PathVariable String userId) {

        var user = userService.changeUserFirstLogin(userId);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/changeUserTags/{userId}")
    public ResponseEntity<UserReturnDto> updateUserTags(@PathVariable String userId, @RequestBody UserTagsDto userTagsDto) {

        var user = userService.updateUserTags(userId, userTagsDto);

        return ResponseEntity.ok(user);
    }
}
