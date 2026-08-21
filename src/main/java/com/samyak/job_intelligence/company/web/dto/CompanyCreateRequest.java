package com.samyak.job_intelligence.company.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyCreateRequest (
        @NotBlank
        @Size(max = 255)
        String canonicalName,

        @NotBlank
        @Size(max = 255)
        String displayName,

        @Size(max = 2048)
        String websiteUrl
) {
}
