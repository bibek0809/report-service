package com.example.reportservice.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * RFC 7807 Problem Details implementation.
 * Standardized error response format.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /** URI identifying the error type */
    private URI type;

    /** Short human-readable summary */
    private String title;

    /** HTTP status code */
    private int status;

    /** Detailed explanation */
    private String detail;

    /** Request instance URI */
    private URI instance;

    /** Timestamp of error occurrence */
    private Instant timestamp;

    /** Error code for programmatic handling */
    private String errorCode;

    /** Request correlation ID */
    private String requestId;

    /** Validation errors */
    private List<FieldError> fieldErrors;

    /** Additional context */
    private Map<String, Object> extensions;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}