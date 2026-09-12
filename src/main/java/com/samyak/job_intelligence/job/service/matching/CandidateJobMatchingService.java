package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.job.domain.JobMatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CandidateJobMatchingService {
    private final JobBatchMatchEvaluationService jobBatchMatchEvaluationService;
    private final JobRankingService jobRankingService;


    public CandidateJobMatchingService(JobBatchMatchEvaluationService jobBatchMatchEvaluationService, JobRankingService jobRankingService) {
        this.jobBatchMatchEvaluationService = jobBatchMatchEvaluationService;
        this.jobRankingService = jobRankingService;
    }

    public List<JobMatch> evaluateAndRank(Long candidateProfileId){
        jobBatchMatchEvaluationService.evaluate(candidateProfileId);
        return jobRankingService.rank(candidateProfileId);
    }
}
