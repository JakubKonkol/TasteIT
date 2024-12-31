package pl.jakubkonkol.tasteitserver.listiner;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.event.NotificationEvent;
import pl.jakubkonkol.tasteitserver.service.interfaces.INotificationService;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final INotificationService notificationService;

    @EventListener
    public void onNotificationRequest(NotificationEvent event) {
        notificationService.sendNotification(
                event.type(),
                event.userId(),
                event.actorId(),
                event.postId()
        );
    }
}