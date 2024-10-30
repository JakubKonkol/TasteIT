package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.service.BadgeService;

@RestController
@RequestMapping("/api/v1/badge")
@RequiredArgsConstructor
public class BadgeController {
    private final BadgeService badgeService;
//    @PatchMapping ("/{badgeId}/{userId}")
//    public ResponseEntity<String> grantBadgeToUser(@PathVariable String badgeId, @PathVariable String userId,
//                                                  @RequestHeader("Authorization") String sessionToken){
//        badgeService.grantBadgeToUser(badgeId, userId, sessionToken);
//        return ResponseEntity.ok(badgeId);
//    }
}
