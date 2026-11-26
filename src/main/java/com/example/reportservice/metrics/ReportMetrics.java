package com.example.reportservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Micrometer metrics for report generation performance.
 * Tracks generation time, throughput, and error rates.
 */
@Component
@Getter
public class ReportMetrics {

    private final Timer generationTimer;
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter compilationCounter;

    public ReportMetrics(MeterRegistry registry) {
        this.generationTimer = Timer.builder("jasper.report.generation")
                .description("Time taken to generate reports")
                .register(registry);

        this.successCounter = Counter.builder("jasper.report.success")
                .description("Successful report generations")
                .register(registry);

        this.errorCounter = Counter.builder("jasper.report.errors")
                .description("Failed report generations")
                .register(registry);

        this.compilationCounter = Counter.builder("jasper.report.compilations")
                .description("Report template compilations")
                .register(registry);
    }
}
