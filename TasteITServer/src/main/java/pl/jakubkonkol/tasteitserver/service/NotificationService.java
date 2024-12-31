package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jakubkonkol.tasteitserver.dto.NotificationDto;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.event.NotificationEvent;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Notification;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;
import pl.jakubkonkol.tasteitserver.repository.NotificationRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.INotificationService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationManager notificationManager;
    private final IUserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(NotificationService.class.getName());


    public List<NotificationDto> getRecentNotifications(String sessionToken, Integer page, Integer size) {
        UserReturnDto user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getUserId(), PageRequest.of(page, size))
                .stream()
                .map(notification -> {
                    UserShort actor = userService.findUserShortByUserId(notification.getActorId());
                    return convertToDto(notification, actor);
                })
                .toList();
    }

    @Transactional
    public void markAsRead(String notificationId, String sessionToken) {
        UserReturnDto user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Cannot mark other user's notifications as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        // Inform client about the update
        messagingTemplate.convertAndSendToUser(
                user.getUserId(),
                "/topic/notifications/read",
                notificationId
        );
    }

    @Transactional
    public void sendNotification(NotificationType type, String userId, String actorId, String postId) {
        notificationManager.handleNotification(new NotificationEvent(type, userId, actorId, postId));
    }

    public long getUnreadNotificationsCount(String sessionToken) {
        UserReturnDto user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        return notificationRepository.countByUserIdAndReadFalse(user.getUserId());
    }

    @Transactional
    public void markAllAsRead(String sessionToken) {
        UserReturnDto user = userService.getCurrentUserDtoBySessionToken(sessionToken);
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalse(user.getUserId());

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);

            // Inform client about each update
            messagingTemplate.convertAndSendToUser(
                    user.getUserId(),
                    "/topic/notifications/read",
                    notification.getNotificationId()
            );
        }
    }

    private NotificationDto convertToDto(Notification notification, UserShort actor) {
        NotificationDto dto = modelMapper.map(notification, NotificationDto.class);
        dto.setMessage(notification.constructMessage(actor.getDisplayName()));
        dto.setActorName(actor.getDisplayName());
        dto.setActorPicture(actor.getProfilePicture());
        return dto;
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs at midnight every day
    public void cleanupOldNotifications() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long readNotificationsDeleted = notificationRepository
                    .deleteByReadTrueAndCreatedAtBefore(thirtyDaysAgo);
            LOGGER.log(Level.INFO, "Cleaned up {} read notifications", readNotificationsDeleted);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to cleanup old notifications", e);
        }
    }
}


