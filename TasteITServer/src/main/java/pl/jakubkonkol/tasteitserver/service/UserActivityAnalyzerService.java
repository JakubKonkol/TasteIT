package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.event.PreferenceUpdateRequiredEvent;
import pl.jakubkonkol.tasteitserver.event.UserActionEvent;
import pl.jakubkonkol.tasteitserver.model.UserActivityInfo;
import pl.jakubkonkol.tasteitserver.model.enums.PreferenceUpdateReason;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserActivityAnalyzerService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
@RequiredArgsConstructor
public class UserActivityAnalyzerService implements IUserActivityAnalyzerService {
    // Publisher for broadcasting events across the application
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, UserActivityInfo> userActivities = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(UserActivityAnalyzerService.class.getName());

    // Number of actions that trigger an automatic preference update (default: 5)
    @Value("${app.activity.analysis.action-threshold:5}")
    private int actionThreshold;

    /**
     * Main entry point for analyzing user activity.
     * Triggered whenever a user performs an action in the system.
     * Analyzes recent activity patterns and triggers preference updates if needed.
     */
    @EventListener
    public void analyzeUserActivity(UserActionEvent event) {
        try {
            String userId = event.userId();

            UserActivityInfo activityInfo = userActivities.computeIfAbsent(
                    userId,
                    k -> new UserActivityInfo()
            );

            activityInfo.incrementCount();

            LOGGER.log(Level.INFO, "User {0} performed action. Current count: {1}",
                    new Object[]{userId, activityInfo.getActionCount()});

            if (activityInfo.shouldTriggerUpdate(actionThreshold)) {
                activityInfo.markUpdateInProgress();

                LOGGER.log(Level.INFO, "Detected significant activity for user {0}", userId);

                eventPublisher.publishEvent(
                        new PreferenceUpdateRequiredEvent(userId, PreferenceUpdateReason.SIGNIFICANT_ACTIVITY)
                );
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO,"Error analyzing user activity", e);
        }
    }

    public void resetUserActivity(String userId) {
        UserActivityInfo activityInfo = userActivities.get(userId);
        if (activityInfo != null) {
            activityInfo.reset();
        }
    }

    public boolean isUpdateInProgress(String userId) {
        UserActivityInfo activityInfo = userActivities.get(userId);
        return activityInfo != null && activityInfo.isUpdateInProgress();
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    private void cleanupActivities() {
        userActivities.clear();
    }
}
