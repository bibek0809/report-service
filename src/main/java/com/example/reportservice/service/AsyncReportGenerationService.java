package com.example.reportservice.service;

import com.example.reportservice.config.JasperProperties;
import com.example.reportservice.exception.ReportException;
import com.example.reportservice.model.ReportFormat;
import com.example.reportservice.model.ReportRequest;
import com.example.reportservice.service.export.AsyncExcelReportExporter;
import com.example.reportservice.service.export.AsyncHtmlReportExporter;
import com.example.reportservice.service.export.AsyncPdfReportExporter;
import com.example.reportservice.service.export.AsyncReportExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fully async report generation service.
 *
 * Pipeline (all non-blocking):
 * 1. Compile template (async cache)
 * 2. Fill with data (virtual thread)
 * 3. Export to format (virtual thread)
 * 4. Return Mono<byte[]> for reactive response
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReportGenerationService {

    private final AsyncReportCompilerService compilerService;
    private final AsyncPdfReportExporter pdfExporter;
    private final AsyncHtmlReportExporter htmlExporter;
    private final AsyncExcelReportExporter excelExporter;
    private final JasperProperties properties;

    /**
     * Generates a report fully asynchronously.
     *
     * Reactive chain:
     * compileAsync → fillAsync → exportAsync → Mono<byte[]>
     *
     * Each step runs on virtual threads, never blocking the event loop.
     */
    public Mono<byte[]> generateReportAsync(ReportRequest request) {
        long startTime = System.currentTimeMillis();

        return Mono.deferContextual(ctx -> {
            log.info("[{}] Starting async report: template={}, format={}",
                    ctx.getOrDefault("requestId", "unknown"),
                    request.getTemplateName(), request.getFormat());

            return compileAndFillAsync(request)
                    .flatMap(jasperPrint -> exportAsync(jasperPrint, request.getFormat()))
                    .timeout(properties.getGenerationTimeout())
                    .doOnSuccess(bytes -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info("[{}] Report generated: {} bytes in {}ms",
                                ctx.getOrDefault("requestId", "unknown"),
                                bytes.length, duration);
                    })
                    .doOnError(error ->
                            log.error("Report generation failed: {}", error.getMessage())
                    )
                    .onErrorMap(throwable -> {
                        if (throwable instanceof ReportException) {
                            return throwable;
                        }
                        return new ReportException(
                                ReportException.ErrorCode.EXPORT_ERROR,
                                "Async generation failed: " + throwable.getMessage(),
                                throwable
                        );
                    });
        });
    }

    /**
     * Combines compilation and filling into single async operation.
     * Uses CompletableFuture.thenCompose for async chaining.
     */
    private Mono<JasperPrint> compileAndFillAsync(ReportRequest request) {
        // Step 1: Async compile (returns CompletableFuture)
        CompletableFuture<JasperReport> compileFuture =
                compilerService.compileReportAsync(request.getTemplateName());

        // Step 2: Convert to Mono and chain with fill
        return Mono.fromFuture(compileFuture)
                .flatMap(report -> fillReportAsync(report, request));
    }

    /**
     * Fills the compiled report with data on a virtual thread.
     */
    private Mono<JasperPrint> fillReportAsync(JasperReport report, ReportRequest request) {
        return Mono.fromCallable(() -> {
                    log.debug("Filling report on virtual thread: {}",
                            Thread.currentThread());

                    JRDataSource dataSource = createDataSource(request.getDataSource());
                    Map<String, Object> parameters = new HashMap<>(request.getParameters());

                    return JasperFillManager.fillReport(report, parameters, dataSource);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Routes to appropriate async exporter.
     */
    private Mono<byte[]> exportAsync(JasperPrint jasperPrint, ReportFormat format) {
        AsyncReportExporter exporter = switch (format) {
            case PDF -> pdfExporter;
            case HTML -> htmlExporter;
            case XLS, XLSX -> excelExporter;
            // Add missing enum values or a default
            default -> throw new ReportException(
                    ReportException.ErrorCode.INVALID_FORMAT,
                    "Unsupported export format: " + format
            );
        };

        return exporter.exportAsync(jasperPrint);
    }

    /**
     * Creates JRDataSource from request data.
     */
    private JRDataSource createDataSource(Object data) {
        if (data == null) {
            return new JREmptyDataSource();
        }
        if (data instanceof JRDataSource jrDataSource) {
            return jrDataSource;
        }
        if (data instanceof List<?> list) {
            return new JRBeanCollectionDataSource(list);
        }
        return new JRBeanCollectionDataSource(List.of(data));
    }

    /**
     * Determines output filename.
     */
    public String getOutputFilename(ReportRequest request) {
        if (request.getOutputFilename() != null && !request.getOutputFilename().isBlank()) {
            return request.getOutputFilename();
        }
        return request.getTemplateName() + request.getFormat().getFileExtension();
    }

    /**
     * Batch generation - process multiple reports concurrently.
     * Virtual threads handle massive concurrency efficiently.
     */
    public Mono<List<byte[]>> generateReportsBatch(List<ReportRequest> requests) {
        List<Mono<byte[]>> monos = requests.stream()
                .map(this::generateReportAsync)
                .toList();

        // Merge all Monos and collect results
        return Mono.zip(monos, objects ->
                java.util.Arrays.stream(objects)
                        .map(o -> (byte[]) o)
                        .toList()
        );
    }
}