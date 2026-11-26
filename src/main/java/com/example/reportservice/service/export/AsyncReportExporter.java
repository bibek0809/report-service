package com.example.reportservice.service.export;

import com.example.reportservice.model.ReportFormat;
import net.sf.jasperreports.engine.JasperPrint;
import reactor.core.publisher.Mono;

/**
 * Async export strategy interface.
 * Returns Mono for reactive, non-blocking export.
 */
public interface AsyncReportExporter {

    /**
     * Exports a filled report asynchronously.
     * Runs on virtual threads, returns Mono<byte[]> for reactive chaining.
     */
    Mono<byte[]> exportAsync(JasperPrint jasperPrint);

    ReportFormat getFormat();
}
