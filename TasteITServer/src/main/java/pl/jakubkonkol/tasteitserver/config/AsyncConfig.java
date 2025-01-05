package pl.jakubkonkol.tasteitserver.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.jakubkonkol.tasteitserver.monitor.ThreadPoolMonitor;
import pl.jakubkonkol.tasteitserver.service.UserPreferencesAnalysisService;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class AsyncConfig {
    @Value("${app.async.post-fetching.thread-pool-size:3}")
    private int postFetchingThreads;

    @Value("${app.async.preference-update.thread-pool-size:4}")
    private int preferenceUpdateThreads;

    @Bean
    public ExecutorService postFetchingExecutorService() {
        return Executors.newFixedThreadPool(
                postFetchingThreads,
                new CustomThreadFactory("post-fetcher")
        );
    }

    @Bean
    public ExecutorService preferenceUpdateExecutorService() {
        return Executors.newFixedThreadPool(
                preferenceUpdateThreads,
                new CustomThreadFactory("preference-updater")
        );
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        // Thread-safe counter for generating unique thread numbers
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private static final Logger LOGGER = Logger.getLogger(CustomThreadFactory.class.getName());


        public CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());

            // Configure as non-daemon to ensure task completion
            thread.setDaemon(false);
            // Set normal priority level
            thread.setPriority(Thread.NORM_PRIORITY);

            thread.setUncaughtExceptionHandler((t, e) ->
                    LOGGER.log(Level.WARNING, "Uncaught exception in thread " + t.getName() + ": " + e.getMessage())
            );

            return thread;
        }
    }

    @Bean
    public ThreadPoolMonitor threadPoolMonitor(
            ExecutorService postFetchingExecutorService,
            ExecutorService preferenceUpdateExecutorService) {
        Map<String, ExecutorService> pools = Map.of(
                "Post Fetching", postFetchingExecutorService,
                "Preference Update", preferenceUpdateExecutorService
        );
        return new ThreadPoolMonitor(pools);
    }
}