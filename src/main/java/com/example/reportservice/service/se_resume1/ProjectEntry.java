package com.example.reportservice.service.se_resume1;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Featured project for the projects section.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntry {

    @NotBlank
    private String name;

    private String description;

    /** Technologies used: "Java, Spring Boot, Kafka, PostgreSQL" */
    private String techStack;

    /** GitHub or live demo URL */
    private String url;
}