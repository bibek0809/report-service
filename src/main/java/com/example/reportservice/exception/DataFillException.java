package com.example.reportservice.exception;

import java.util.Map;

/**
 * Thrown when filling a report with data fails.
 * Common causes: type mismatches, missing fields, null values in non-null expressions.
 */
public class DataFillException extends ReportException {

    private final String fieldName;
    private final Object offendingValue;
    private final Map<String, Object> availableParameters;

    public DataFillException(String message) {
        super(ErrorCode.DATA_FILL_ERROR, message);
        this.fieldName = null;
        this.offendingValue = null;
        this.availableParameters = null;
    }

    public DataFillException(String fieldName, Object offendingValue, String message) {
        super(ErrorCode.DATA_FILL_ERROR,
                "Data fill error in field '" + fieldName + "': " + message);
        this.fieldName = fieldName;
        this.offendingValue = offendingValue;
        this.availableParameters = null;
    }

    public DataFillException(String message, Map<String, Object> availableParameters) {
        super(ErrorCode.DATA_FILL_ERROR, message);
        this.fieldName = null;
        this.offendingValue = null;
        this.availableParameters = availableParameters;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getOffendingValue() {
        return offendingValue;
    }

    public Map<String, Object> getAvailableParameters() {
        return availableParameters;
    }
}
