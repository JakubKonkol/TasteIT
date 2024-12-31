package pl.jakubkonkol.tasteitserver.event;

import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;

public record NotificationEvent (NotificationType type, String userId, String actorId, String postId){
}
