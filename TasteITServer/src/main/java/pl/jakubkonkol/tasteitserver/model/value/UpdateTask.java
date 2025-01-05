package pl.jakubkonkol.tasteitserver.model.value;

import lombok.Value;
import pl.jakubkonkol.tasteitserver.model.enums.PreferenceUpdateReason;

import java.time.LocalDateTime;

public record UpdateTask(
        String userId,
        PreferenceUpdateReason reason,
        LocalDateTime createdAt
) {
    public UpdateTask(String userId, PreferenceUpdateReason reason) {
        this(userId, reason, LocalDateTime.now());
    }
}