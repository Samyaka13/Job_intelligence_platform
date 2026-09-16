package com.samyak.job_intelligence.candidate.web.dto;

public record CandidateSkillBulkItemResponse(
        String skill,
        CandidateSkillBulkStatus status
) {
}
