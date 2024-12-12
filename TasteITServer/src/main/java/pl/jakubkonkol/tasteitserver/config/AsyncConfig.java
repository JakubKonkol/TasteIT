package pl.jakubkonkol.tasteitserver.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AsyncConfig {

    @Bean
    public ExecutorService postFetchingExecutorService() {
        return Executors.newFixedThreadPool(
                3,
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        return new Thread(r, "post-fetcher-" + threadNumber.getAndIncrement());
                    }
                }
        );
    }
}