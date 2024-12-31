package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.NotificationDto;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;

import java.util.List;

public interface INotificationService {
    void sendNotification(NotificationType type, String userId,
                          String actorId, String postId);
    List<NotificationDto> getRecentNotifications(String sessionToken,
                                                 Integer page, Integer size);
    void markAsRead(String notificationId, String sessionToken);
    long getUnreadNotificationsCount(String sessionToken);
    void markAllAsRead(String sessionToken);
}
