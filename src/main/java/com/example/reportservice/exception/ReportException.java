package com.example.reportservice.exception;

import lombok.Getter;

import java.time.Instant;

/**
 * Base exception for all report generation errors.
 * Structured error codes enable programmatic error handling
 * and client-side retry logic.
 */
@Getter
public class ReportException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Instant timestamp;
    private final String requestId;

    public ReportException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.requestId = null;
    }

    public ReportException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.requestId = null;
    }

    public ReportException(ErrorCode errorCode, String message, String requestId) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.requestId = requestId;
    }

    public ReportException(ErrorCode errorCode, String message, Throwable cause, String requestId) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.requestId = requestId;
    }

    /**
     * Categorized error codes for client handling.
     */
    public enum ErrorCode {
        /** Template file not found in configured path */
        TEMPLATE_NOT_FOUND,

        /** JRXML syntax error or compilation failure */
        TEMPLATE_COMPILE_ERROR,

        /** Data binding error during report filling */
        DATA_FILL_ERROR,

        /** Export to target format failed */
        EXPORT_ERROR,

        /** Unsupported output format requested */
        INVALID_FORMAT,

        /** Report generation exceeded timeout */
        TIMEOUT_ERROR,

        /** Insufficient memory for large report */
        MEMORY_ERROR,

        /** Concurrent modification or race condition */
        CONCURRENCY_ERROR,

        /** Invalid parameter type or missing required param */
        INVALID_PARAMETER,

        /** Data source connection or read failure */
        DATA_SOURCE_ERROR,

        /** Cache corruption or disk I/O failure */
        CACHE_ERROR
    }
}
