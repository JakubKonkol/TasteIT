package pl.jakubkonkol.tasteitserver.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;

import java.time.LocalDateTime;

@Document
@Data
@Builder
public class Notification {
    @Id
    private String notificationId;
    private String userId;
    private String actorId;
    private String postId;
    private NotificationType type;
    private String message;
    private boolean read;
    @CreatedDate
    private LocalDateTime createdAt;

    public String constructMessage(String actorName) {
        return switch (type) {
            case NEW_FOLLOWER -> actorName + " started following you";
            case POST_LIKE -> actorName + " liked your post";
            case POST_COMMENT -> actorName + " commented on your post";
        };
    }
}