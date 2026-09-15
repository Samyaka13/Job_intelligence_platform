package com.samyak.job_intelligence.candidate.web.dto;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;

import java.math.BigDecimal;

public record CandidateSkillResponse(
        Long id,
        Long candidateProfileId,
        String skill,
        String normalizedSkill,
        String proficiency,
        BigDecimal yearsExperience,
        boolean primary
) {

    public static CandidateSkillResponse from(CandidateSkill skill) {
        return new CandidateSkillResponse(
                skill.getId(),
                skill.getCandidateProfile().getId(),
                skill.getSkill(),
                skill.getNormalizedSkill(),
                skill.getProficiency(),
                skill.getYearsExperience(),
                skill.isPrimary()
        );
    }
}