package com.samyak.job_intelligence.candidate.web.dto;

import java.util.List;

public record CandidateSkillBulkResponse(
        int created,
        int duplicates,
        List<CandidateSkillBulkItemResponse> results
) {
}
