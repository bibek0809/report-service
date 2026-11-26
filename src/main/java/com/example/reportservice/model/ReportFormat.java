package com.example.reportservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported document export formats.
 * Each format defines MIME type and file extension for HTTP responses.
 */
@Getter
@RequiredArgsConstructor
public enum ReportFormat {

    /** Portable Document Format - best for printing and archiving */
    PDF("application/pdf", ".pdf"),

    /** HyperText Markup Language - best for web viewing */
    HTML("text/html", ".html"),

    /** Excel 97-2003 format - legacy Excel compatibility */
    XLS("application/vnd.ms-excel", ".xls"),

    /** Excel Open XML format - modern Excel format */
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),

    /** Comma-Separated Values - for data interchange */
    CSV("text/csv", ".csv"),

    /** Rich Text Format - for word processors */
    RTF("application/rtf", ".rtf"),

    /** Plain text */
    TXT("text/plain", ".txt"),

    /** XML representation of the report */
    XML("application/xml", ".xml");

    private final String mimeType;
    private final String fileExtension;

    /**
     * Case-insensitive deserialization from JSON.
     */
    @JsonCreator
    public static ReportFormat fromString(String format) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Report format cannot be null or empty");
        }
        try {
            return valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported report format: '" + format + "'. " +
                            "Supported: " + java.util.Arrays.toString(values())
            );
        }
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    /**
     * Checks if format is Excel-based.
     */
    public boolean isExcel() {
        return this == XLS || this == XLSX;
    }

    /**
     * Checks if format is web-friendly (viewable in browser).
     */
    public boolean isWebViewable() {
        return this == HTML || this == PDF;
    }
}
