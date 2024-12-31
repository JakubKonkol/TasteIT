package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.event.NotificationEvent;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishNotification(NotificationType type, String userId,
                                    String actorId, String postId) {
        NotificationEvent event = new NotificationEvent(type, userId, actorId, postId);
        eventPublisher.publishEvent(event);
    }
}