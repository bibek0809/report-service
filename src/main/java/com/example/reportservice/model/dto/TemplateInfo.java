package com.example.reportservice.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Metadata about an available report template.
 */
@Data
@Builder
public class TemplateInfo {

    private String name;
    private String description;
    private List<String> supportedFormats;
    private List<ParameterInfo> parameters;
    private List<FieldInfo> fields;
    private Instant lastModified;
    private long fileSize;
    private boolean compiled;

    @Data
    @Builder
    public static class ParameterInfo {
        private String name;
        private String type;
        private boolean required;
        private String description;
        private Object defaultValue;
    }

    @Data
    @Builder
    public static class FieldInfo {
        private String name;
        private String type;
        private String description;
    }
}
