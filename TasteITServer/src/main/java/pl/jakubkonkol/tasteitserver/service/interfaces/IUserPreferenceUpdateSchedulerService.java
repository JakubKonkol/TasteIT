package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.event.PreferenceUpdateRequiredEvent;

public interface IUserPreferenceUpdateSchedulerService {
    void initialize();
    void scheduleFullUpdate();
    void processUpdateQueue();
    void handlePreferenceUpdateRequired(PreferenceUpdateRequiredEvent event);
}
