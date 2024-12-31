package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.NotificationDto;
import pl.jakubkonkol.tasteitserver.event.NotificationEvent;
import pl.jakubkonkol.tasteitserver.model.Notification;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;
import pl.jakubkonkol.tasteitserver.repository.NotificationRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotificationManager {
    private final IUserService userService;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(NotificationManager.class.getName());

    public void handleNotification(NotificationEvent event) {
        Notification notification = saveNotification(
                event.type(),
                event.userId(),
                event.actorId(),
                event.postId()
        );

        try {
            sendRealtimeNotification(notification);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Failed to send real-time notification, but notification was saved", e);
        }
    }

    private Notification saveNotification(NotificationType type, String userId,
                                          String actorId, String postId) {
        Notification notification = Notification.builder()
                .type(type)
                .userId(userId)
                .actorId(actorId)
                .postId(postId)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    private void sendRealtimeNotification(Notification notification) {
        var actor = userService.findUserShortByUserId(notification.getActorId());
        NotificationDto notificationDto = modelMapper.map(notification, NotificationDto.class);
        notificationDto.setMessage(notification.constructMessage(actor.getDisplayName()));
        notificationDto.setActorName(actor.getDisplayName());
        notificationDto.setActorPicture(actor.getProfilePicture());

        messagingTemplate.convertAndSendToUser(
                notification.getUserId(),
                "/topic/notifications",
                notificationDto
        );
    }
}
