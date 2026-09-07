package com.samyak.job_intelligence.job.service.matching;

import java.math.BigDecimal;
import java.time.Instant;

public record JobMatchPersistenceData(
        boolean hardQualified,
        BigDecimal skillScore,
        BigDecimal experienceScore,
        BigDecimal roleScore,
        BigDecimal locationScore,
        BigDecimal salaryScore,
        BigDecimal semanticScore,
        BigDecimal finalScore,
        String matchReasoning,
        String llmProvider,
        String llmModel,
        String llmPromptVersion,
        Instant evaluatedAt
) {
}