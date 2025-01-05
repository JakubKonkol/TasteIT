package pl.jakubkonkol.tasteitserver.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.event.PreferenceUpdateRequiredEvent;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.enums.PreferenceUpdateReason;
import pl.jakubkonkol.tasteitserver.model.value.UpdateTask;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserActivityAnalyzerService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserPreferenceUpdateSchedulerService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserPreferencesAnalysisService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class UserPreferenceUpdateSchedulerService implements IUserPreferenceUpdateSchedulerService {
    private final IUserPreferencesAnalysisService preferencesAnalysisService;
    private final IUserActivityAnalyzerService userActivityAnalyzerService;
    private final IUserService userService;

    // Service for recording metrics and monitoring system performance
    private final MeterRegistry meterRegistry;

    // Thread pool for parallel processing of preference updates
    private final ExecutorService preferenceUpdateExecutorService;
    private static final Logger LOGGER = Logger.getLogger(UserPreferenceUpdateSchedulerService.class.getName());

    /**
     * Thread-safe queue for storing update tasks.
     * Using BlockingQueue ensures thread safety when multiple producers/consumers access the queue
     */
    private final BlockingQueue<UpdateTask> updateQueue = new LinkedBlockingQueue<>();

    /**
     * Maximum number of tasks to process in a single batch
     * Default value is 10, can be configured via application properties
     */
    @Value("${app.preference-update.batch-size:10}")
    private int batchSize;

    /**
     * Initializes metrics monitoring system.
     * Called automatically after bean construction.
     */
    @PostConstruct
    public void initialize() {
        setupMetrics();
    }

    /**
     * Scheduled job that runs daily at 2 AM to update all active users' preferences.
     */
    @Scheduled(cron = "${app.preference-update.full-update-cron:0 0 2 * * *}")
    public void scheduleFullUpdate(){
        LOGGER.log(Level.INFO, "Starting scheduled full update of user preferences");

        try {
            List<User> activeUsers = userService.findUsersActiveInLast30Days();

            activeUsers.forEach(user -> {
                if (!userActivityAnalyzerService.isUpdateInProgress(user.getUserId())) {
                    addToQueue(new UpdateTask(user.getUserId(), PreferenceUpdateReason.SCHEDULED_UPDATE));
                } else {
                    LOGGER.log(Level.INFO, "Skipping user {0} - update already in progress", user.getUserId());
                }
            });

            LOGGER.log(Level.INFO, "Scheduled update for {0} users", activeUsers.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error scheduling full update", e);
        }
    }

    /**
     * Processes queued update tasks in batches every 10 minutes (configurable).
     * Uses batch processing for better efficiency and resource utilization.
     */
    @Scheduled(fixedRateString = "${app.preference-update.processing-interval-ms:600000}")
    public void processUpdateQueue() {
        List<UpdateTask> batch = new ArrayList<>();
        // Drain up to batchSize tasks from queue
        updateQueue.drainTo(batch, batchSize);

        if(!batch.isEmpty()) {
            LOGGER.log(Level.INFO, "Processing batch of {0} preference updates", batch.size());
            Timer timer = meterRegistry.timer("preference_update_single_processing");

            timer.record(() -> {
                try {
                    // Process each task in the batch asynchronously
                    List<CompletableFuture<Void>> futures = batch.stream()
                            .map(this::processUpdate)
                            .toList();

                    // Wait for all tasks to complete (with timeout)
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .get(5, TimeUnit.MINUTES);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing update batch", e);
                    // Re-queue failed tasks for retry
                    batch.forEach(this::addToQueue);
                }
            });
        }
    }

    /**
     * Processes a single update task asynchronously.
     * Uses ExecutorService for controlled parallel execution.
     */
    private CompletableFuture<Void> processUpdate(UpdateTask task) {
        return CompletableFuture.runAsync(() -> {
            Timer timer = meterRegistry.timer("preference_update_single_processing");
            timer.record(() -> {
                try {
                    LOGGER.log(Level.INFO, "Processing preference update for user {0}", task.userId());
                    preferencesAnalysisService.requestPreferenceAnalysis(task.userId());
                    meterRegistry.counter("preference_updates_successful").increment();
                    userActivityAnalyzerService.resetUserActivity(task.userId());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing update for user " + task.userId() + " error: " + e);
                    meterRegistry.counter("preference_updates_failed").increment();
                    addToQueue(task); // Retry failed task
                }
            });
        }, preferenceUpdateExecutorService);
    }

    /**
     * Handles preference update requests triggered by significant user activity.
     * Adds new update tasks to the processing queue.
     */
    @EventListener
    public void handlePreferenceUpdateRequired(PreferenceUpdateRequiredEvent event) {
        addToQueue(new UpdateTask(event.userId(), event.reason()));
    }

    /**
     * Safely adds a task to the queue with retry mechanism.
     * If queue is full, attempts blocking put operation.
     */
    private void addToQueue(UpdateTask task) {
        if(!updateQueue.offer(task)) {
            LOGGER.log(Level.WARNING,
                    "Failed to add task for user {} to queue - queue might be full",
                    task.userId());
            try {
                updateQueue.put(task);  // Blocking operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Interrupted while adding task to queue", e);
            }
        }
    }

    /**
     * Sets up monitoring metrics for the queue.
     * Registers a gauge that tracks queue size.
     */
    private void setupMetrics() {
        Gauge.builder("update_queue_size", updateQueue, Queue::size)
                .description("Number of updates waiting in the queue")
                .register(meterRegistry);
    }
}
