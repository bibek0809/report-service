package com.example.reportservice.service.resume_basic;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * One job entry. `highlights` is a bullet list in the API payload but the
 * template only needs a single pre-joined string, so we expose a derived
 * getter (`getHighlightsText`) purely for JasperReports field binding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceEntry {

    @NotBlank
    private String company;

    private String role;
    private String location;

    /** Pre-formatted, e.g. "Jan 2022 – Present" */
    private String dateRange;

    private String techStack;


    @Builder.Default
    private List<String> highlights = Collections.emptyList();

    /** Field JasperReports binds to: "• bullet 1\n• bullet 2 ..." */
    public String getHighlightsText() {
        if (highlights == null || highlights.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highlights.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("•  ").append(highlights.get(i));
        }
        return sb.toString();
    }
}
