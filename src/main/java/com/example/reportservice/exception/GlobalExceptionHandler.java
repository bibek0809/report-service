package com.example.reportservice.exception;

import com.example.reportservice.model.error.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler producing RFC 7807 Problem Details.
 * Maps all domain exceptions to appropriate HTTP status codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI BASE_TYPE = URI.create("https://api.example.com/errors");

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ApiError> handleTemplateNotFound(TemplateNotFoundException e) {
        log.warn("Template not found: {}", e.getMessage());

        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/template-not-found"))
                .title("Report Template Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(e.getMessage())
                .timestamp(e.getTimestamp())
                .errorCode(e.getErrorCode().name())
                .requestId(e.getRequestId())
                .extensions(Map.of(
                        "templateName", e.getTemplateName(),
                        "searchedPath", e.getSearchedPath() != null ? e.getSearchedPath() : "default"
                ))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TemplateCompilationException.class)
    public ResponseEntity<ApiError> handleCompilationError(TemplateCompilationException e) {
        log.error("Template compilation error: {}", e.getMessage(), e);

        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/compilation-error"))
                .title("Template Compilation Failed")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .detail(e.getMessage())
                .timestamp(e.getTimestamp())
                .errorCode(e.getErrorCode().name())
                .extensions(Map.of(
                        "templateName", e.getTemplateName(),
                        "lineNumber", e.getLineNumber()
                ))
                .build();

        return ResponseEntity.unprocessableEntity().body(error);
    }

    @ExceptionHandler(DataFillException.class)
    public ResponseEntity<ApiError> handleDataFillError(DataFillException e) {
        log.error("Data fill error: {}", e.getMessage());

        ApiError.ApiErrorBuilder builder = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/data-fill-error"))
                .title("Report Data Binding Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(e.getMessage())
                .timestamp(e.getTimestamp())
                .errorCode(e.getErrorCode().name());

        if (e.getFieldName() != null) {
            builder.extensions(Map.of(
                    "fieldName", e.getFieldName(),
                    "offendingValue", e.getOffendingValue() != null ? e.getOffendingValue().toString() : "null"
            ));
        }

        return ResponseEntity.badRequest().body(builder.build());
    }

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<ApiError> handleExportError(ExportException e) {
        log.error("Export error: {}", e.getMessage(), e);

        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/export-error"))
                .title("Report Export Failed")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(e.getMessage())
                .timestamp(e.getTimestamp())
                .errorCode(e.getErrorCode().name())
                .extensions(Map.of(
                        "targetFormat", e.getTargetFormat().name(),
                        "pageCount", e.getPageCount()
                ))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ReportTimeoutException.class)
    public ResponseEntity<ApiError> handleTimeout(ReportTimeoutException e) {
        log.warn("Report timeout: {}", e.getMessage());

        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/timeout"))
                .title("Report Generation Timeout")
                .status(HttpStatus.REQUEST_TIMEOUT.value())
                .detail(e.getMessage())
                .timestamp(e.getTimestamp())
                .errorCode(e.getErrorCode().name())
                .extensions(Map.of(
                        "timeoutMs", e.getTimeout().toMillis(),
                        "templateName", e.getTemplateName(),
                        "underlyingError", e.getUnderlyingError() != null ? e.getUnderlyingError().name() : null
                ))
                .build();

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(InvalidParameterException e) {
        log.warn("Invalid parameter: {}", e.getMessage());

        ApiError.ApiErrorBuilder builder = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/invalid-parameter"))
                .title("Invalid Report Parameter")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(e.getMessage())
                .timestamp(Instant.now())
                .errorCode(e.getErrorCode().name());

        if (e.getMissingParameters() != null) {
            builder.extensions(Map.of("missingParameters", e.getMissingParameters()));
        } else if (e.getParameterName() != null) {
            builder.extensions(Map.of(
                    "parameterName", e.getParameterName(),
                    "expectedType", e.getExpectedType() != null ? e.getExpectedType().getName() : null,
                    "actualType", e.getActualType() != null ? e.getActualType().getName() : null
            ));
        }

        return ResponseEntity.badRequest().body(builder.build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        List<ApiError.FieldError> fieldErrors = e.getFieldErrors().stream()
                .map(error -> ApiError.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/validation-error"))
                .title("Request Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more fields failed validation")
                .timestamp(Instant.now())
                .errorCode("VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException e) {
        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/request-error"))
                .title(e.getReason() != null ? e.getReason() : "Request Error")
                .status(e.getStatusCode().value())
                .detail(e.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(e.getStatusCode()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e) {
        log.error("Unexpected error", e);

        ApiError error = ApiError.builder()
                .type(URI.create(BASE_TYPE + "/internal-error"))
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .timestamp(Instant.now())
                .errorCode("INTERNAL_ERROR")
                .build();

        return ResponseEntity.internalServerError().body(error);
    }
}
