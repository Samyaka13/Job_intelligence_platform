package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.repository.JobMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobRankingService {
    private final JobMatchRepository jobMatchRepository;

    public JobRankingService(JobMatchRepository jobMatchRepository) {
        this.jobMatchRepository = jobMatchRepository;
    }

    public List<JobMatch> rank(Long candidateProfileId){
        return jobMatchRepository.findByCandidateProfileIdAndHardQualifiedTrueOrderByFinalScoreDescEvaluatedAtDesc(candidateProfileId);
    }
}
