package com.example.reportservice.service.se_resume1;

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
import net.sf.jasperreports.export.type.PdfVersionEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Professional software engineer resume generation service.
 * Uses the modern two-column template with skill bars and project showcase.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SoftwareEngineerResumeService {

    private static final String TEMPLATE_NAME = "software_engineer_resume";

    private final AsyncReportCompilerService compilerService;

    /**
     * Generates a professional software engineer resume PDF.
     */
    public Mono<byte[]> generateResumePdf(ResumeRequestNew request) {
        long startTime = System.currentTimeMillis();

        return Mono.fromFuture(compilerService.compileReportAsync(TEMPLATE_NAME))
                .flatMap(report -> fillResume(report, request))
                .flatMap(this::exportToPdf)
                .doOnSuccess(bytes ->
                        log.info("SE Resume generated for '{}': {} bytes in {}ms",
                                request.getFullName(), bytes.length,
                                System.currentTimeMillis() - startTime))
                .doOnError(err -> log.error("SE Resume generation failed: {}", err.getMessage()))
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

    public String getOutputFilename(ResumeRequestNew request) {
        String safeName = request.getFullName() == null
                ? "resume"
                : request.getFullName().trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
        return safeName + "_se_resume.pdf";
    }

    private Mono<JasperPrint> fillResume(JasperReport report, ResumeRequestNew request) {
        return Mono.fromCallable(() -> {
                    log.debug("Filling SE resume template on virtual thread: {}", Thread.currentThread());
                    Map<String, Object> params = buildParameters(request);
                    return JasperFillManager.fillReport(report, params, new JREmptyDataSource());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Map<String, Object> buildParameters(ResumeRequestNew request) {
        Map<String, Object> params = new HashMap<>();

        // Header
        params.put("P_FULL_NAME", nullToEmpty(request.getFullName()));
        params.put("P_JOB_TITLE", nullToEmpty(request.getJobTitle()));

        // Contact
        params.put("P_EMAIL", nullToEmpty(request.getEmail()));
        params.put("P_PHONE", nullToEmpty(request.getPhone()));
        params.put("P_LOCATION", nullToEmpty(request.getLocation()));
        params.put("P_LINKEDIN", nullToEmpty(request.getLinkedIn()));
        params.put("P_GITHUB", nullToEmpty(request.getGithub()));
        params.put("P_WEBSITE", nullToEmpty(request.getWebsite()));

        // Summary
        params.put("P_SUMMARY", nullToEmpty(request.getSummary()));

        // Data sources with JRBeanCollectionDataSource
        params.put("P_EXPERIENCE", new JRBeanCollectionDataSource(
                orEmpty(request.getExperience())));
        params.put("P_EDUCATION", new JRBeanCollectionDataSource(
                orEmpty(request.getEducation())));
        params.put("P_PROJECTS", new JRBeanCollectionDataSource(
                orEmpty(request.getProjects())));
        params.put("P_SKILLS", new JRBeanCollectionDataSource(
                orEmpty(request.getSkills())));
        params.put("P_CERTIFICATIONS", new JRBeanCollectionDataSource(
                orEmpty(request.getCertifications())));

        // Text fields
        params.put("P_LANGUAGES", nullToEmpty(request.getLanguages()));
        params.put("P_INTERESTS", nullToEmpty(request.getInterests()));

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
                    exportConfig.setPdfVersion(PdfVersionEnum.VERSION_1_7);
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
}