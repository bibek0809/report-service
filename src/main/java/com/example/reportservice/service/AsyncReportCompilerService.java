package com.example.reportservice.service;


import com.example.reportservice.config.JasperProperties;
import com.example.reportservice.exception.ReportException;
import com.example.reportservice.exception.TemplateNotFoundException;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Async report compiler with Caffeine async cache.
 * Compilation runs on virtual threads without blocking callers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReportCompilerService {

    private final JasperProperties properties;
    private final ResourceLoader resourceLoader;
    private final Executor compilationExecutor;  // Virtual thread executor

    /**
     * Async cache for compiled reports.
     * Uses Caffeine's async loading for non-blocking cache access.
     */
    private AsyncCache<String, JasperReport> reportCache;

    @PostConstruct
    public void initializeCache() {
        var builder = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .recordStats();

        if (properties.isCacheEnabled()) {
            builder.removalListener((key, value, cause) ->
                    log.debug("Removed report '{}' from cache: {}", key, cause));
        }

        reportCache = builder.buildAsync();
        log.info("Async report cache initialized");
    }

    /**
     * Asynchronously compiles a report template.
     * Returns immediately with a CompletableFuture.
     *
     * Flow:
     * 1. Check async cache (non-blocking)
     * 2. If miss, compile on virtual thread
     * 3. Cache the result asynchronously
     */
    public CompletableFuture<JasperReport> compileReportAsync(String templateName) {
        // Try async cache first
        return reportCache.get(templateName, (name, executor) ->
                CompletableFuture.supplyAsync(() -> doCompile(name), executor)
        );
    }

    /**
     * Synchronous wrapper for compatibility.
     * Blocks only if compilation is in progress.
     */
    public JasperReport compileReport(String templateName) {
        try {
            return compileReportAsync(templateName).get(
                    properties.getGenerationTimeout().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } catch (Exception e) {
            throw new ReportException(
                    ReportException.ErrorCode.TEMPLATE_COMPILE_ERROR,
                    "Compilation failed for: " + templateName,
                    e
            );
        }
    }

    /**
     * Actual compilation logic - runs on virtual thread.
     */
    private JasperReport doCompile(String templateName) {
        // Check disk cache first
        Path cachedFile = properties.getCompiledReportCachePath()
                .resolve(templateName + ".jasper");

        if (properties.isCacheEnabled() && Files.exists(cachedFile)) {
            try (InputStream is = Files.newInputStream(cachedFile)) {
                log.debug("Loading cached report: {}", templateName);
                return (JasperReport) JRLoader.loadObject(is);
            } catch (Exception e) {
                log.warn("Cache load failed, recompiling: {}", e.getMessage());
            }
        }

        // Compile from source
        log.info("Compiling report on virtual thread: {}",
                Thread.currentThread());

        String templatePath = properties.getTemplatePath() + templateName + ".jrxml";

        try {
            Resource resource = resourceLoader.getResource(templatePath);
            if (!resource.exists()) {
                throw new TemplateNotFoundException(templateName);
            }

            JasperReport report;
            try (InputStream is = resource.getInputStream()) {
                report = JasperCompileManager.compileReport(is);
            }

            // Async cache to disk
            if (properties.isCacheEnabled()) {
                try {
                    Files.createDirectories(cachedFile.getParent());
                    JRSaver.saveObject(report, cachedFile.toString());
                } catch (Exception e) {
                    log.warn("Failed to cache compiled report: {}", e.getMessage());
                }
            }

            log.info("Compiled report: {} ({} bytes)", templateName,
                    cachedFile.toFile().length());
            return report;

        } catch (IOException e) {
            throw new ReportException(
                    ReportException.ErrorCode.TEMPLATE_COMPILE_ERROR,
                    "Failed to read template: " + templateName,
                    e
            );
        } catch (JRException e) {
            throw new ReportException(
                    ReportException.ErrorCode.TEMPLATE_COMPILE_ERROR,
                    "Failed to compile template: " + templateName,
                    e
            );
        }
    }

    /**
     * Clears all caches asynchronously.
     */
    public CompletableFuture<Void> clearCacheAsync() {
        return CompletableFuture.runAsync(() -> {
            reportCache.synchronous().invalidateAll();
            log.info("Async cache cleared");
        }, compilationExecutor);
    }
}
