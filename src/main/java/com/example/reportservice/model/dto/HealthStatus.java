package com.example.reportservice.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Service health and performance metrics.
 */
@Data
@Builder
public class HealthStatus {

    private String status;
    private Instant timestamp;
    private String version;
    private boolean virtualThreadsEnabled;

    private Map<String, Object> threadPoolStats;
    private Map<String, Object> cacheStats;
    private Map<String, Object> memoryStats;

    @Data
    @Builder
    public static class ThreadPoolStats {
        private long activeThreads;
        private long queuedTasks;
        private long completedTasks;
        private boolean usingVirtualThreads;
    }
}
