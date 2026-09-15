package com.samyak.job_intelligence.candidate.web.dto;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record CandidateResponse(
        Long id,
        String name,
        String email,
        String currentLocation,
        JsonNode preferredLocations,
        BigDecimal minimumSalary,
        String minimumSalaryCurrency,
        boolean active,
        BigDecimal experienceYears,
        JsonNode preferredEmploymentTypes
) {

    public static CandidateResponse from(CandidateProfile candidateProfile){
        return new CandidateResponse(
                candidateProfile.getId(),
                candidateProfile.getName(),
                candidateProfile.getEmail(),
                candidateProfile.getCurrentLocation(),
                candidateProfile.getPreferredLocations(),
                candidateProfile.getMinimumSalary(),
                candidateProfile.getMinimumSalaryCurrency(),
                candidateProfile.isActive(),
                candidateProfile.getExperienceYears(),
                candidateProfile.getPreferredEmploymentTypes()
        );
    }
}
