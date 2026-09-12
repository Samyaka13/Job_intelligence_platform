package com.samyak.job_intelligence.job.service.matching;


import com.samyak.job_intelligence.job.service.qualification.JobQualificationResult;
import com.samyak.job_intelligence.job.service.qualification.JobQualificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobMatchingService {

    private final JobQualificationService jobQualificationService;
    private final SkillMatchingService skillMatchingService;
    private final SkillMatchScoringService skillMatchScoringService;
    private final JobOverallScoringService jobOverallScoringService;


    public JobMatchingService(JobQualificationService jobQualificationService, SkillMatchingService skillMatchingService, SkillMatchScoringService skillMatchScoringService,JobOverallScoringService jobOverallScoringService) {
        this.jobQualificationService = jobQualificationService;
        this.skillMatchingService = skillMatchingService;
        this.skillMatchScoringService = skillMatchScoringService;
        this.jobOverallScoringService = jobOverallScoringService;
    }

    public JobMatchResult match(JobMatchingInput jobMatchingInput){
        JobQualificationResult jobQualificationResult = jobQualificationService.qualify(jobMatchingInput.job(),jobMatchingInput.candidateProfile(),jobMatchingInput.locations());
        if(!jobQualificationResult.qualified()){
            return new JobMatchResult(false,
                    jobQualificationResult.rejectionReasons(),
                    null,
                    null,
                    null
                    );
        }
        SkillMatchResult skillMatchResult = skillMatchingService.match(jobMatchingInput.candidateProfile().getId(),jobMatchingInput.job().getId());
        SkillMatchScore skillMatchScore = skillMatchScoringService.calculate(skillMatchResult);
        JobOverallScore overallScore =
                jobOverallScoringService.calculate(skillMatchScore);
        return new JobMatchResult(true,
                List.of(),
                skillMatchScore,
                skillMatchResult,
                overallScore
                );
    }
}
