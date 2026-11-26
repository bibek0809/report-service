package com.example.reportservice.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Builder with validation for ReportRequest.
 * Ensures all requests are valid before processing.
 */
public class ReportRequestBuilder {

    private static final Validator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    public static ReportRequest buildValidated(
            String templateName,
            ReportFormat format,
            java.util.Map<String, Object> parameters,
            Object dataSource) {

        ReportRequest request = ReportRequest.builder()
                .templateName(templateName)
                .format(format)
                .parameters(parameters != null ? parameters : java.util.Map.of())
                .dataSource(dataSource)
                .requestId(java.util.UUID.randomUUID().toString())
                .build();

        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }

        return request;
    }
}