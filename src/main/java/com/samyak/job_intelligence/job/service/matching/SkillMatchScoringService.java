package com.samyak.job_intelligence.job.service.matching;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SkillMatchScoringService {
    public SkillMatchScore calculate(SkillMatchResult matchResult){
        if(matchResult.totalRequirements() == 0){
            return new SkillMatchScore(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false
            );
        }

        BigDecimal matchedRequirementRatio = BigDecimal.valueOf(matchResult.matchedSkill().size()).divide(BigDecimal.valueOf(matchResult.totalRequirements()),
                4,
                RoundingMode.HALF_UP); //0.3333

        BigDecimal mandatoryMatchRatio;

        if (matchResult.mandatoryRequirements() == 0) {
            mandatoryMatchRatio = BigDecimal.ZERO;
        } else {
            int matchedMandatoryRequirements =
                    matchResult.mandatoryRequirements()
                            - matchResult.missingMandatorySkills().size();//1

            mandatoryMatchRatio =
                    BigDecimal.valueOf(matchedMandatoryRequirements)
                            .divide(
                                    BigDecimal.valueOf(
                                            matchResult.mandatoryRequirements()
                                    ),
                                    4,
                                    RoundingMode.HALF_UP
                            );//0.5000
        }
        BigDecimal score =
                matchedRequirementRatio.multiply(BigDecimal.valueOf(0.4))
                        .add(mandatoryMatchRatio.multiply(BigDecimal.valueOf(0.6))).setScale(4,RoundingMode.HALF_UP);

        return new SkillMatchScore(
                score,
                matchedRequirementRatio,
                mandatoryMatchRatio,
                !matchResult.missingMandatorySkills().isEmpty()
        );
    }
}
