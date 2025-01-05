package pl.jakubkonkol.tasteitserver.monitor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ThreadPoolMonitor {
    private final Map<String, ExecutorService> threadPools;
    private static final Logger LOGGER = Logger.getLogger(ThreadPoolMonitor.class.getName());

    public ThreadPoolMonitor(Map<String, ExecutorService> threadPools) {
        this.threadPools = threadPools;
    }

    // Monitors the status of all thread pools every minute.
    @Scheduled(fixedRate = 60000)
    public void monitorThreadPools() {
        threadPools.forEach((name, pool) -> {
            if (pool instanceof ThreadPoolExecutor executor) {
                LOGGER.log(Level.INFO,
                        "{0} Pool Status: active={1}, completed={2}, queued={3}",
                        new Object[]{
                                name,
                                executor.getActiveCount(),          // Currently running tasks
                                executor.getCompletedTaskCount(),   // Finished tasks
                                executor.getQueue().size()          // Waiting tasks
                        }
                );
            }
        });
    }
}
