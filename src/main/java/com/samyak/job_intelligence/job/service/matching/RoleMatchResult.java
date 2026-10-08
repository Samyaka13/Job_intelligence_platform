package com.samyak.job_intelligence.job.service.matching;

import java.math.BigDecimal;

public record RoleMatchResult(
        JobRoleFamily jobRoleFamily,
        BigDecimal roleScore,
        BigDecimal seniorityScore
) {
}
