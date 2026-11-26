package com.example.reportservice.service.se_resume1;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Technical skill with visual proficiency level (0-100).
 * Rendered as a progress bar in the resume sidebar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillEntry {

    @NotBlank
    private String name;

    /** Proficiency level 0-100 for visual skill bar */
    @Min(0)
    @Max(100)
    @Builder.Default
    private Integer level = 80;

    /** Category for grouping: "Languages", "Frameworks", "Tools", "Cloud" */
    private String category;
}