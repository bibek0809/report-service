package com.example.reportservice.service.resume_basic;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry {

    @NotBlank
    private String institution;

    private String degree;
    private String location;

    /** Pre-formatted, e.g. "2016 – 2020" */
    private String dateRange;

    /** e.g. "GPA 3.8/4.0, Dean's List" — optional, may be blank */
    private String details;
    private String gpa;


}
