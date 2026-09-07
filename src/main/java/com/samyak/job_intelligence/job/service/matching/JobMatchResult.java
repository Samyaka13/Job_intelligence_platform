package com.samyak.job_intelligence.job.service.matching;

import java.util.List;

public record JobMatchResult(
        boolean qualified,
                             List<String> qualificationRejectionReasons,
                             SkillMatchScore skillMatchScore,
                             SkillMatchResult skillMatchResult,
                             JobOverallScore jobOverallScore
) {
}
