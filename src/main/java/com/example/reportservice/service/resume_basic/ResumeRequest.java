package com.example.reportservice.service.resume_basic;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequest {

    @NotBlank
    private String fullName;

    private String jobTitle;

    @Email
    private String email;

    private String phone;
    private String location;
    private String linkedIn;
    private String website;

    private String summary;

    @NotEmpty
    private List<@Valid ExperienceEntry> experience;

    @NotEmpty
    private List<@Valid EducationEntry> education;

    /** Simple flat list, e.g. ["Java", "Spring Boot", "Kafka"] */
    private List<String> skills;

    /** Optional, e.g. ["AWS Certified Solutions Architect — Amazon (2023)"] */
    private List<String> certifications;
}