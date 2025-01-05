package pl.jakubkonkol.tasteitserver.event;

import lombok.Getter;
import pl.jakubkonkol.tasteitserver.model.enums.PreferenceUpdateReason;

import java.time.LocalDateTime;

public record PreferenceUpdateRequiredEvent(
        String userId,
        LocalDateTime timestamp,
        PreferenceUpdateReason reason
) {
    public PreferenceUpdateRequiredEvent(String userId, PreferenceUpdateReason reason) {
        this(userId, LocalDateTime.now(), reason);
    }
}
