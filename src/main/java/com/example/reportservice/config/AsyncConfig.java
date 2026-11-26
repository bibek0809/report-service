package com.example.reportservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Configures virtual thread executors for async operations.
 * Virtual threads are lightweight (KBs vs MBs) and auto-scheduled
 * by the JVM on platform threads.
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Primary async executor using virtual threads.
     * Handles thousands of concurrent report generations
     * without thread pool exhaustion.
     */
    @Bean
    @Primary
    public AsyncTaskExecutor virtualThreadAsyncExecutor() {
        // Create a named virtual thread factory for observability
        ThreadFactory factory = Thread.ofVirtual()
                .name("report-vt-", 1)
                .factory();

        Executor executor = Executors.newThreadPerTaskExecutor(factory);

        log.info("Virtual thread executor initialized for report generation");
        return new TaskExecutorAdapter(executor);
    }

    /**
     * Backup platform thread executor for CPU-bound compilation.
     * Compilation is CPU-intensive, so we limit concurrent compiles
     * to prevent resource starvation.
     */
    @Bean(name = "compilationExecutor")
    public ThreadPoolTaskExecutor compilationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("jasper-compile-");
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return virtualThreadAsyncExecutor();
    }
}
