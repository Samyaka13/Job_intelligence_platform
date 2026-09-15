package com.samyak.job_intelligence.candidate.web.dto;

import jakarta.validation.constraints.*;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record CandidateCreateRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @Size(max = 255)
        String currentLocation,

        @NotNull
        JsonNode preferredLocations,

        @PositiveOrZero
        BigDecimal minimumSalary,

        @Size(max = 10)
        String minimumSalaryCurrency,

        @NotNull
        @PositiveOrZero
        BigDecimal experienceYears,

        @NotNull
        JsonNode preferredEmploymentTypes

) {
}
