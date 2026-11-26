package com.example.reportservice.exception;

import java.time.Duration;

/**
 * Thrown when report generation exceeds the configured timeout.
 * Client should retry or increase timeout for complex reports.
 */
public class ReportTimeoutException extends ReportException {

    private final Duration timeout;
    private final String templateName;
    private final ReportException.ErrorCode underlyingError;

    public ReportTimeoutException(Duration timeout, String templateName) {
        super(ErrorCode.TIMEOUT_ERROR,
                "Report generation for '" + templateName +
                        "' exceeded timeout of " + timeout.toMillis() + "ms");
        this.timeout = timeout;
        this.templateName = templateName;
        this.underlyingError = null;
    }

    public ReportTimeoutException(Duration timeout, String templateName,
                                  ReportException.ErrorCode underlyingError) {
        super(ErrorCode.TIMEOUT_ERROR,
                "Report generation for '" + templateName +
                        "' exceeded timeout of " + timeout.toMillis() + "ms. " +
                        "Underlying error: " + underlyingError);
        this.timeout = timeout;
        this.templateName = templateName;
        this.underlyingError = underlyingError;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public String getTemplateName() {
        return templateName;
    }

    public ReportException.ErrorCode getUnderlyingError() {
        return underlyingError;
    }
}
