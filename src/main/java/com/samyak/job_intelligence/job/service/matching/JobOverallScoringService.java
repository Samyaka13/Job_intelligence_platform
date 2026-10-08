package com.samyak.job_intelligence.job.service.matching;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class JobOverallScoringService {
    public JobOverallScore calculate(SkillMatchScore skillMatchScore){
        if(skillMatchScore == null) return new JobOverallScore(null);

        BigDecimal finalScore = skillMatchScore.score()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return new JobOverallScore(finalScore);
    }

    public JobOverallScore calculate(SkillMatchScore skillMatchScore, RoleMatchResult roleMatchResult){
        if (skillMatchScore == null || roleMatchResult == null) {
            return new JobOverallScore(null, null, null);
        }

        BigDecimal technical = skillMatchScore.score().multiply(BigDecimal.valueOf(100));
        BigDecimal score = technical.multiply(new BigDecimal("0.45"))
                .add(roleMatchResult.roleScore().multiply(new BigDecimal("0.40")))
                .add(roleMatchResult.seniorityScore().multiply(new BigDecimal("0.15")));

        // Missing an explicit technical must-have should be visible in ranking,
        // but must not turn a potentially useful stretch job into a hard rejection.
        if (skillMatchScore.hasMissingMandatorySkills()) {
            score = score.multiply(new BigDecimal("0.45"));
        }

        return new JobOverallScore(
                score.setScale(2, RoundingMode.HALF_UP),
                roleMatchResult.roleScore(),
                roleMatchResult.seniorityScore()
        );


    }
}
