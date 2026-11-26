package com.example.reportservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Externalized configuration with async-specific settings.
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "jasper")
public class JasperProperties {

    @NotBlank
    private String templatePath = "classpath:/reports/";

    @NotNull
    private Path compiledReportCachePath = Path.of(
            System.getProperty("java.io.tmpdir"), "jasper-cache"
    );

    private boolean cacheEnabled = true;

    @NotNull
    private Duration generationTimeout = Duration.ofSeconds(30);

    private String defaultEncoding = "UTF-8";

    /**
     * Max concurrent virtual threads for report generation.
     * 0 = unlimited (virtual threads scale automatically).
     */
    private int maxConcurrentGenerations = 0;

    /**
     * Enable non-blocking compilation with virtual threads.
     */
    private boolean asyncCompilation = true;
}
