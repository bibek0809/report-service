package com.example.reportservice.service.se_resume1;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Education entry with GPA support.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry {

    @NotBlank
    private String institution;

    @NotBlank
    private String degree;

    private String location;
    private String dateRange;
    private String gpa;
}