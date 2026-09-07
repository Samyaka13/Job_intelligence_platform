package com.samyak.job_intelligence.job.service.matching;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Component
public class JobMatchPersistenceMapper {

    public JobMatchPersistenceData map(
            JobMatchResult matchResult,
            Instant evaluatedAt
    ) {
        if (!matchResult.qualified()) {
            return new JobMatchPersistenceData(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    java.math.BigDecimal.ZERO,
                    String.join("; ", matchResult.qualificationRejectionReasons()),
                    null,
                    null,
                    null,
                    evaluatedAt
            );
        }

        BigDecimal skillScore =
                matchResult.skillMatchScore()
                        .score()
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new JobMatchPersistenceData(
                true,
                skillScore,
                null,
                null,
                null,
                null,
                null,
                matchResult.jobOverallScore().finalScore(),
                buildMatchReasoning(matchResult),
                null,
                null,
                null,
                evaluatedAt
        );
    }

    private String buildMatchReasoning(JobMatchResult matchResult) {

        SkillMatchResult skillMatchResult =
                matchResult.skillMatchResult();

        return "Matched skills: "
                + String.join(", ", skillMatchResult.matchedSkill())
                + "; Missing skills: "
                + String.join(", ", skillMatchResult.missingSkill());
    }
}