package com.example.reportservice.controller;

import com.example.reportservice.model.ReportFormat;
import com.example.reportservice.model.ReportRequest;
import com.example.reportservice.model.dto.TestEmployeeReport;
import com.example.reportservice.service.AsyncReportGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.*;

/**
 * Reactive REST controller for non-blocking report generation.
 * Uses WebFlux for async request/response handling.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReactiveReportController {

    private final AsyncReportGenerationService reportService;

    /**
     * Generates a report asynchronously and streams the response.
     *
     * Non-blocking flow:
     * 1. Receive JSON request
     * 2. Start async generation (virtual thread)
     * 3. Return immediately, stream bytes when ready
     * 4. Never blocks the Netty event loop
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Void> generateReport(@Valid @RequestBody Mono<ReportRequest> requestMono,ServerHttpResponse response) {

        String requestId = UUID.randomUUID().toString();

        return requestMono
                .flatMap(request ->
                        reportService.generateReportAsync(request)
                                .contextWrite(Context.of("requestId", requestId))
                                .flatMap(bytes -> {
                                    // Set response headers
                                    response.getHeaders().setContentType(
                                            MediaType.parseMediaType(request.getFormat().getMimeType())
                                    );
                                    response.getHeaders().setContentDispositionFormData(
                                            "attachment",
                                            reportService.getOutputFilename(request)
                                    );
                                    response.getHeaders().set("X-Request-Id", requestId);

                                    // Stream bytes as DataBuffer
                                    DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
                                    return response.writeWith(Flux.just(buffer));
                                })
                )
                .onErrorResume(error -> {
                    log.error("Report generation error: {}", error.getMessage());
                    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return response.setComplete();
                });
    }

    /**
     * Streaming endpoint - generates and streams chunks.
     * Useful for very large reports to start download immediately.
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<DataBuffer> generateReportStream(@Valid @RequestBody ReportRequest request) {
        String requestId = UUID.randomUUID().toString();

        return reportService.generateReportAsync(request)
                .contextWrite(Context.of("requestId", requestId))
                .flatMapMany(bytes -> {
                    // Split large reports into chunks for streaming
                    int chunkSize = 8192;
                    DefaultDataBufferFactory factory = new DefaultDataBufferFactory();

                    return Flux.create(sink -> {
                        for (int i = 0; i < bytes.length; i += chunkSize) {
                            int end = Math.min(i + chunkSize, bytes.length);

                            byte[] chunk = Arrays.copyOfRange(bytes, i, end);
                            sink.next(factory.wrap(chunk));
                        }
                        sink.complete();
                    });
                });
    }

    /**
     * Batch generation - multiple reports concurrently.
     * Virtual threads handle thousands of concurrent requests.
     */
    @PostMapping("/generate/batch")
    public Flux<byte[]> generateReportsBatch(@RequestBody java.util.List<ReportRequest> requests) {
        return Flux.fromIterable(requests)
                .flatMap(reportService::generateReportAsync);
    }

    /**
     * Simple GET endpoint for quick generation.
     */
    @GetMapping("/{templateName}.{format}")
    public Mono<byte[]> generateReportSimple(
            @PathVariable String templateName,
            @PathVariable String format,
            @RequestParam Map<String, String> parameters) {

        try {
            ReportRequest request = ReportRequest.builder()
                    .templateName(templateName)
                    .format(ReportFormat.fromString(format))
                    .parameters(new java.util.HashMap<>(parameters))
                    .build();

            return reportService.generateReportAsync(request);
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * Health check with virtual thread info.
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        return Mono.fromCallable(() -> Map.of(
                "status", "UP",
                "virtualThreads", true,
                "thread", Thread.currentThread().toString(),
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    @PostMapping(value = "/generate-test", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Void> generateReportTest(ServerHttpResponse response) {

        String requestId = UUID.randomUUID().toString();
        ReportRequest request = buildSampleRequest();

        return reportService.generateReportAsync(request)
                .contextWrite(Context.of("requestId", requestId))
                .flatMap(bytes -> {

                    response.getHeaders().setContentType(
                            MediaType.parseMediaType(request.getFormat().getMimeType())
                    );

                    response.getHeaders().setContentDispositionFormData(
                            "attachment",
                            reportService.getOutputFilename(request)
                    );

                    response.getHeaders().set("X-Request-Id", requestId);
                    response.getHeaders().setContentLength(bytes.length);

                    DataBuffer buffer = response.bufferFactory().wrap(bytes);

                    return response.writeWith(Mono.just(buffer));
                });
    }

    public ReportRequest buildSampleRequest() {
        List<TestEmployeeReport> employees = List.of(
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000),
                new TestEmployeeReport("Ada Lovelace", "Engineering", 125_000),
                new TestEmployeeReport("Grace Hopper", "Engineering", 132_000),
                new TestEmployeeReport("Alan Turing", "Research", 128_500),
                new TestEmployeeReport("Margaret Hamilton", "Engineering", 130_000)
        );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Employee Report");
        parameters.put("GeneratedBy", "Report Service");

        return ReportRequest.builder()
                .reportTitle("Employee Report")
                .format(ReportFormat.PDF)
                .templateName("employee_report")
                .dataSource(employees)
                .parameters(parameters)
                .build();
    }
}
