package com.example.reportservice.service.export;


import com.example.reportservice.model.ReportFormat;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;

/**
 * Async PDF exporter using virtual threads.
 * Export runs off the event loop to prevent blocking.
 */
@Slf4j
@Component
public class AsyncPdfReportExporter implements AsyncReportExporter {

    @Override
    public Mono<byte[]> exportAsync(JasperPrint jasperPrint) {
        return Mono.fromCallable(() -> {
                    log.debug("Exporting PDF on virtual thread: {}",
                            Thread.currentThread());

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    JRPdfExporter exporter = new JRPdfExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                    SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
                    reportConfig.setSizePageToContent(true);

                    SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
                    exportConfig.setCompressed(true);

                    exporter.setConfiguration(reportConfig);
                    exporter.setConfiguration(exportConfig);
                    exporter.exportReport();

                    log.debug("PDF export complete: {} bytes", outputStream.size());
                    return outputStream.toByteArray();
                })
                .subscribeOn(Schedulers.boundedElastic());  // Run on virtual thread pool
    }

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.PDF;
    }
}
