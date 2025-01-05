package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.event.UserActionEvent;

public interface IUserActivityAnalyzerService {
    void analyzeUserActivity(UserActionEvent event);
    void resetUserActivity(String userId);
    boolean isUpdateInProgress(String userId);
}
