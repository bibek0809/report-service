package com.example.reportservice.service.se_resume1;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Professional certification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationEntry {

    @NotBlank
    private String name;

    private String issuer;

    private String year;
}