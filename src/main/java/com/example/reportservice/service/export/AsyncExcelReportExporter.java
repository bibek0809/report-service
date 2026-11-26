package com.example.reportservice.service.export;


import com.example.reportservice.model.ReportFormat;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;

@Slf4j
@Component
public class AsyncExcelReportExporter implements AsyncReportExporter {

    @Override
    public Mono<byte[]> exportAsync(JasperPrint jasperPrint) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setOnePagePerSheet(false);
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setDetectCellType(true);
            config.setCollapseRowSpan(true);

            exporter.setConfiguration(config);
            exporter.exportReport();

            return outputStream.toByteArray();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.XLSX;
    }
}
