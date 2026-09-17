package com.samyak.job_intelligence.job.service.digest;

import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.service.matching.CandidateJobMatchingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Service
@Transactional
public class DailyDigestService {
    private final CandidateJobMatchingService candidateJobMatchingService;


    public DailyDigestService(CandidateJobMatchingService candidateJobMatchingService) {
        this.candidateJobMatchingService = candidateJobMatchingService;
    }

    @Transactional
    public DailyDigest generate(Long candidateProfileId,int limit){
        List<JobMatch> rankedMatches = candidateJobMatchingService.evaluateAndRank(candidateProfileId);

        List<JobMatch> matches = rankedMatches.stream().limit(limit).toList();
        return new DailyDigest(candidateProfileId, Instant.now(),matches);
     }
}
