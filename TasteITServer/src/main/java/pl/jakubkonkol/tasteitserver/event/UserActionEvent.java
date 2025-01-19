package pl.jakubkonkol.tasteitserver.event;

import pl.jakubkonkol.tasteitserver.model.UserAction;

import java.time.LocalDateTime;

public record UserActionEvent(String userId, UserAction action, LocalDateTime timestamp) {
    public UserActionEvent(String userId, UserAction action) {
        this(userId, action, LocalDateTime.now());
    }
}
