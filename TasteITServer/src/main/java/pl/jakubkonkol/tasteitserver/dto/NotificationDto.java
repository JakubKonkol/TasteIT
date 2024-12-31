package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private String notificationId;
    private NotificationType type;
    private String message;
    private String actorName;
    private String actorPicture;
    private String postId;
    private LocalDateTime createdAt;
    private boolean read;
}