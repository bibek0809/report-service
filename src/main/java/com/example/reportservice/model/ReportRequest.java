package com.example.reportservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Map;

/**
 * Request payload for asynchronous report generation.
 * Contains template specification, data parameters, and output configuration.
 */
@Data
@Builder
@JsonDeserialize(builder = ReportRequest.ReportRequestBuilder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportRequest {

    /**
     * Name of the report template (without .jrxml extension).
     * Must exist in the configured template path.
     */
    @NotBlank(message = "Template name is required")
    private String templateName;

    /**
     * Output format for the generated document.
     * Supported: PDF, HTML, XLS, XLSX, CSV, RTF, TXT, XML
     */
    @NotNull(message = "Report format is required")
    private ReportFormat format;

    /**
     * Dynamic parameters injected into the report template.
     * Keys must match parameter names defined in the JRXML.
     * Values are serialized using Jackson and passed to JasperReports.
     */
    @Builder.Default
    private Map<String, Object> parameters = Map.of();

    /**
     * Data source for report detail bands.
     * Supported types:
     * - null: parameter-only report (no detail band)
     * - List&lt;?&gt;: JRBeanCollectionDataSource
     * - Single Object: wrapped in singleton list
     * - JRDataSource: passed directly
     */
    private Object dataSource;

    /**
     * Custom filename for the downloaded document.
     * If omitted, defaults to "{templateName}{extension}".
     */
    private String outputFilename;

    /**
     * Optional locale for localized report rendering.
     * Defaults to system locale if not specified.
     */
    private String locale;

    /**
     * Optional report title override.
     * Injected as $P{REPORT_TITLE} if template supports it.
     */
    private String reportTitle;

    /**
     * Enable/disable pagination for HTML output.
     * Only applicable when format is HTML.
     */
    @Builder.Default
    private boolean paginateHtml = true;

    /**
     * Request correlation ID for distributed tracing.
     * Auto-generated if not provided.
     */
    private String requestId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ReportRequestBuilder {
        // Lombok generates the builder body; this annotation
        // just tells Jackson how to use it (no-prefix setters).
    }
}