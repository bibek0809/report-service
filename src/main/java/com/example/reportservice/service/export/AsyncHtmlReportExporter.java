package com.example.reportservice.service.export;

import com.example.reportservice.model.ReportFormat;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;

/**
 * Async HTML exporter.
 */
@Slf4j
@Component
public class AsyncHtmlReportExporter implements AsyncReportExporter {

    @Override
    public Mono<byte[]> exportAsync(JasperPrint jasperPrint) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));

            SimpleHtmlExporterConfiguration config = new SimpleHtmlExporterConfiguration();
            config.setFlushOutput(true);
            exporter.setConfiguration(config);
            exporter.exportReport();

            return outputStream.toByteArray();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.HTML;
    }
}
