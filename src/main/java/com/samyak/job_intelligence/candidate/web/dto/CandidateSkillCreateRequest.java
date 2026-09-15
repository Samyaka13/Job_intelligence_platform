package com.samyak.job_intelligence.candidate.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CandidateSkillCreateRequest(
        @NotBlank
        @Size(max = 255)
        String skill,

        @Size(max = 30)
        String proficiency,

        @PositiveOrZero
        BigDecimal yearsExperience,

        boolean primary
) {
}
