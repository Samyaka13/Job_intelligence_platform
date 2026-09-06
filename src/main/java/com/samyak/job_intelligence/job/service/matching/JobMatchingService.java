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


    public JobMatchingService(JobQualificationService jobQualificationService, SkillMatchingService skillMatchingService, SkillMatchScoringService skillMatchScoringService) {
        this.jobQualificationService = jobQualificationService;
        this.skillMatchingService = skillMatchingService;
        this.skillMatchScoringService = skillMatchScoringService;
    }

    public JobMatchResult match(JobMatchingInput jobMatchingInput){
        JobQualificationResult jobQualificationResult = jobQualificationService.qualify(jobMatchingInput.job(),jobMatchingInput.candidateProfile());
        if(!jobQualificationResult.qualified()){
            return new JobMatchResult(false,
                    jobQualificationResult.rejectionReasons(),
                    null,
                    null
                    );
        }
        SkillMatchResult skillMatchResult = skillMatchingService.match(jobMatchingInput.candidateProfile().getId(),jobMatchingInput.jobId());
        SkillMatchScore skillMatchScore = skillMatchScoringService.calculate(skillMatchResult);
        return new JobMatchResult(true,
                List.of(),
                skillMatchScore,
                skillMatchResult
                );
    }
}
