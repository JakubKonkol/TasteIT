package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.NotificationDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.interfaces.INotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final INotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getRecent(
            @RequestHeader("Authorization") String sessionToken,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<NotificationDto> recentNotifications = notificationService.getRecentNotifications(sessionToken, page, size);
        return ResponseEntity.ok(recentNotifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<GenericResponse> markAsRead(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String sessionToken) {
        notificationService.markAsRead(notificationId, sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Notification read")
                .build());
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@RequestHeader("Authorization") String sessionToken) {
        long count = notificationService.getUnreadNotificationsCount(sessionToken);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/read-all")
    public ResponseEntity<GenericResponse> markAllAsRead(
            @RequestHeader("Authorization") String sessionToken) {
        notificationService.markAllAsRead(sessionToken);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("All notifications marked as read")
                .build());
    }
}
