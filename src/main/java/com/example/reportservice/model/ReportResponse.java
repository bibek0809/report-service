package com.example.reportservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

/**
 * Metadata response for report generation status.
 * Returned when generation is accepted but not yet complete (async polling).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    /** Unique request identifier */
    private String requestId;

    /** Current status of the report generation */
    private Status status;

    /** Template name being processed */
    private String templateName;

    /** Target output format */
    private ReportFormat format;

    /** Time when request was accepted */
    private Instant acceptedAt;

    /** Time when generation completed (if done) */
    private Instant completedAt;

    /** Processing duration */
    private Duration duration;

    /** Download URL for completed reports */
    private String downloadUrl;

    /** Error details if generation failed */
    private ErrorDetails error;

    /** Report size in bytes (if completed) */
    private Long sizeBytes;

    /** Number of pages generated */
    private Integer pageCount;

    public enum Status {
        ACCEPTED,      // Request received, queued
        COMPILING,     // Template compilation in progress
        FILLING,       // Data binding in progress
        EXPORTING,     // Format conversion in progress
        COMPLETED,     // Ready for download
        FAILED         // Error occurred
    }

    @Data
    @Builder
    public static class ErrorDetails {
        private String code;
        private String message;
        private String detail;
    }
}