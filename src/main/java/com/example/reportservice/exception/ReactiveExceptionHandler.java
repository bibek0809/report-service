package com.example.reportservice.exception;

import com.example.reportservice.model.error.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Reactive exception handler for WebFlux endpoints.
 * Returns Mono<ResponseEntity> for non-blocking error responses.
 */
@Slf4j
@RestControllerAdvice
public class ReactiveExceptionHandler {

    private static final URI BASE_TYPE = URI.create("https://api.example.com/errors");

    @ExceptionHandler(TemplateNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleTemplateNotFound(TemplateNotFoundException e) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.builder()
                        .type(URI.create(BASE_TYPE + "/template-not-found"))
                        .title("Template Not Found")
                        .status(HttpStatus.NOT_FOUND.value())
                        .detail(e.getMessage())
                        .timestamp(e.getTimestamp())
                        .errorCode(e.getErrorCode().name())
                        .extensions(Map.of("templateName", e.getTemplateName()))
                        .build()));
    }

    @ExceptionHandler(ReportException.class)
    public Mono<ResponseEntity<ApiError>> handleReportException(ReportException e) {
        HttpStatus status = switch (e.getErrorCode()) {
            case TEMPLATE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TEMPLATE_COMPILE_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            case DATA_FILL_ERROR, INVALID_FORMAT, INVALID_PARAMETER -> HttpStatus.BAD_REQUEST;
            case TIMEOUT_ERROR -> HttpStatus.REQUEST_TIMEOUT;
            case MEMORY_ERROR -> HttpStatus.INSUFFICIENT_STORAGE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return Mono.just(ResponseEntity.status(status)
                .body(ApiError.builder()
                        .type(URI.create(BASE_TYPE + "/report-error"))
                        .title("Report Generation Failed")
                        .status(status.value())
                        .detail(e.getMessage())
                        .timestamp(e.getTimestamp())
                        .errorCode(e.getErrorCode().name())
                        .requestId(e.getRequestId())
                        .build()));
    }


    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiError>> handleResponseStatus(ResponseStatusException e) {
        return Mono.just(ResponseEntity.status(e.getStatusCode())
                .body(ApiError.builder()
                        .title(e.getReason())
                        .status(e.getStatusCode().value())
                        .detail(e.getMessage())
                        .timestamp(Instant.now())
                        .build()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGeneric(Exception e) {
        log.error("Unexpected reactive error", e);
        return Mono.just(ResponseEntity.internalServerError()
                .body(ApiError.builder()
                        .type(URI.create(BASE_TYPE + "/internal-error"))
                        .title("Internal Server Error")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .detail("An unexpected error occurred")
                        .timestamp(Instant.now())
                        .build()));
    }
}
