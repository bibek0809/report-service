package com.example.reportservice.service.resume_basic;


import com.example.reportservice.exception.ReportException;
import com.example.reportservice.service.AsyncReportCompilerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mirrors the compile -> fill -> export pipeline used by ReportServiceImpl,
 * but specialized for the resume template (single-record data source,
 * everything driven off parameters + two nested List components).
 *
 * Wire `compilerService` to your existing CompilerService bean (the one
 * that exposes `compileReportAsync(String templateName)` and handles the
 * disk cache) so the resume template benefits from the same caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final String TEMPLATE_NAME = "resume_template";

    private final AsyncReportCompilerService compilerService;


    public Mono<byte[]> generateResumePdf(ResumeRequest request) {
        long startTime = System.currentTimeMillis();

        return Mono.fromFuture(compilerService.compileReportAsync(TEMPLATE_NAME))
                .flatMap(report -> fillResume(report, request))
                .flatMap(this::exportToPdf)
                .doOnSuccess(bytes ->
                        log.info("Resume generated for '{}': {} bytes in {}ms",
                                request.getFullName(), bytes.length,
                                System.currentTimeMillis() - startTime))
                .doOnError(err -> log.error("Resume generation failed: {}", err.getMessage()))
                .onErrorMap(throwable -> {
                    if (throwable instanceof ReportException) {
                        return throwable;
                    }
                    return new ReportException(
                            ReportException.ErrorCode.EXPORT_ERROR,
                            "Resume generation failed: " + throwable.getMessage(),
                            throwable);
                });
    }

    public String getOutputFilename(ResumeRequest request) {
        String safeName = request.getFullName() == null
                ? "resume"
                : request.getFullName().trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
        return safeName + "_resume.pdf";
    }

    private Mono<JasperPrint> fillResume(JasperReport report, ResumeRequest request) {
        return Mono.fromCallable(() -> {
                    log.debug("Filling resume template on virtual thread: {}", Thread.currentThread());
                    Map<String, Object> params = buildParameters(request);
                    return JasperFillManager.fillReport(report, params, new JREmptyDataSource());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Map<String, Object> buildParameters(ResumeRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("P_FULL_NAME", nullToEmpty(request.getFullName()));
        params.put("P_JOB_TITLE", nullToEmpty(request.getJobTitle()));
        params.put("P_EMAIL", nullToEmpty(request.getEmail()));
        params.put("P_PHONE", nullToEmpty(request.getPhone()));
        params.put("P_LOCATION", nullToEmpty(request.getLocation()));
        params.put("P_LINKEDIN", nullToEmpty(request.getLinkedIn()));
        params.put("P_WEBSITE", nullToEmpty(request.getWebsite()));
        params.put("P_SUMMARY", nullToEmpty(request.getSummary()));

        params.put("P_EXPERIENCE", new JRBeanCollectionDataSource(
                orEmpty(request.getExperience())));
        params.put("P_EDUCATION", new JRBeanCollectionDataSource(
                orEmpty(request.getEducation())));

        params.put("P_SKILLS_TEXT", String.join("   |   ", orEmpty(request.getSkills())));
        params.put("P_CERTIFICATIONS_TEXT", String.join("\n", orEmpty(request.getCertifications())));

        return params;
    }

    private Mono<byte[]> exportToPdf(JasperPrint jasperPrint) {
        return Mono.fromCallable(() -> {
                    JRPdfExporter exporter = new JRPdfExporter();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                    SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
                    reportConfig.setSizePageToContent(true);
                    exporter.setConfiguration(reportConfig);

                    SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
                    exportConfig.setCompressed(true);
                    exporter.setConfiguration(exportConfig);

                    exporter.exportReport();
                    return outputStream.toByteArray();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * Minimal shape of your existing CompilerService so this file compiles
     * standalone. Delete this and inject your real CompilerService instead —
     * the important part is that compileReportAsync("resume_template")
     * resolves /templates/resume_template.jrxml the same way your other
     * templates do.
     */
    public interface CompilerServiceLike {
        java.util.concurrent.CompletableFuture<JasperReport> compileReportAsync(String templateName);
    }
}
