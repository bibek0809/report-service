package com.example.reportservice.service.se_resume1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceEntry {

    private String company;
    private String role;
    private String location;
    private String dateRange;
    private List<String> highlights;

    private String techStack;
}