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
}
