package com.example.reportservice.exception;

import java.util.List;

/**
 * Thrown when report parameters are invalid or missing required values.
 */
public class InvalidParameterException extends ReportException {

    private final String parameterName;
    private final Class<?> expectedType;
    private final Class<?> actualType;
    private final List<String> missingParameters;

    public InvalidParameterException(String parameterName, String message) {
        super(ErrorCode.INVALID_PARAMETER, message);
        this.parameterName = parameterName;
        this.expectedType = null;
        this.actualType = null;
        this.missingParameters = null;
    }

    public InvalidParameterException(String parameterName,
                                     Class<?> expectedType,
                                     Class<?> actualType) {
        super(ErrorCode.INVALID_PARAMETER,
                "Parameter '" + parameterName + "' type mismatch. " +
                        "Expected: " + expectedType.getSimpleName() +
                        ", Actual: " + actualType.getSimpleName());
        this.parameterName = parameterName;
        this.expectedType = expectedType;
        this.actualType = actualType;
        this.missingParameters = null;
    }

    public InvalidParameterException(List<String> missingParameters) {
        super(ErrorCode.INVALID_PARAMETER,
                "Missing required parameters: " + missingParameters);
        this.parameterName = null;
        this.expectedType = null;
        this.actualType = null;
        this.missingParameters = missingParameters;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Class<?> getExpectedType() {
        return expectedType;
    }

    public Class<?> getActualType() {
        return actualType;
    }

    public List<String> getMissingParameters() {
        return missingParameters;
    }
}
