package com.example.reportservice.service.se_resume1;

import com.example.reportservice.service.resume_basic.EducationEntry;
import com.example.reportservice.service.resume_basic.ExperienceEntry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Enhanced resume request for software engineer template.
 * Supports skills with proficiency levels, projects, GitHub, and more.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequestNew {
    @NotBlank
    private String fullName;

    private String jobTitle;

    @Email
    private String email;

    private String phone;
    private String location;
    private String linkedIn;
    private String github;        // NEW: GitHub profile
    private String website;

    private String summary;

    @NotEmpty
    private List<@Valid ExperienceEntry> experience;

    @NotEmpty
    private List<@Valid EducationEntry> education;

    /** Skills with proficiency levels (0-100) */
    private List<@Valid SkillEntry> skills;

    /** Featured projects with tech stacks */
    private List<@Valid ProjectEntry> projects;

    /** Certifications with issuer and year */
    private List<@Valid CertificationEntry> certifications;

    /** Languages spoken, e.g. "English (Native), Spanish (Fluent)" */
    private String languages;

    /** Personal interests */
    private String interests;
}
