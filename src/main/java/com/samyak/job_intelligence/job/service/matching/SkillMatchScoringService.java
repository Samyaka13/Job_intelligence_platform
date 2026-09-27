package com.samyak.job_intelligence.job.service.matching;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SkillMatchScoringService {

    public SkillMatchScore calculate(SkillMatchResult matchResult) {

        if (matchResult.totalRequirements() == 0) {
            return new SkillMatchScore(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false
            );
        }

        BigDecimal matchedRequirementRatio =
                BigDecimal.valueOf(matchResult.matchedRequirements())
                        .divide(
                                BigDecimal.valueOf(matchResult.totalRequirements()),
                                4,
                                RoundingMode.HALF_UP
                        );

        BigDecimal mandatoryMatchRatio;

        if (matchResult.mandatoryRequirements() == 0) {
            mandatoryMatchRatio = BigDecimal.ZERO;
        } else {
            mandatoryMatchRatio =
                    BigDecimal.valueOf(matchResult.matchedMandatoryRequirements())
                            .divide(
                                    BigDecimal.valueOf(matchResult.mandatoryRequirements()),
                                    4,
                                    RoundingMode.HALF_UP
                            );
        }

        BigDecimal score =
                matchedRequirementRatio
                        .multiply(new BigDecimal("0.4"))
                        .add(
                                mandatoryMatchRatio
                                        .multiply(new BigDecimal("0.6"))
                        )
                        .setScale(4, RoundingMode.HALF_UP);

        return new SkillMatchScore(
                score,
                matchedRequirementRatio,
                mandatoryMatchRatio,
                !matchResult.missingMandatorySkills().isEmpty()
        );
    }
}