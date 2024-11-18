package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.service.BadgeService;

@RestController
@RequestMapping("/api/v1/badge")
@RequiredArgsConstructor
public class BadgeController {
    private final BadgeService badgeService;

    @PatchMapping("/{badgeId}")
    public ResponseEntity<String> updateBadgeProgress(@PathVariable String badgeId, @RequestHeader("Authorization") String sessionToken) {
        badgeService.updateBadgeProgress(sessionToken, badgeId);
        return ResponseEntity.ok(badgeId);

    }
}
