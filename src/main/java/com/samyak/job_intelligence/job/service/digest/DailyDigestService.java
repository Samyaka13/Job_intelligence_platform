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

        List<JobMatch> matches = rankedMatches.stream().filter(this::isMainMatch).limit(limit).toList();
        List<JobMatch> stretchMatches = rankedMatches.stream().filter(this::isStretchMatch).limit(limit).toList();
        return new DailyDigest(candidateProfileId, Instant.now(), matches, stretchMatches);
     }

    private boolean isMainMatch(JobMatch match) {
        // Matches calculated before role-aware scoring are kept visible until the
        // scheduled recomputation refreshes them.
        if (match.getSemanticScore() == null || match.getRoleScore() == null) {
            return true;
        }
        return score(match.getSemanticScore()) >= 60 && score(match.getRoleScore()) >= 60 && score(match.getFinalScore()) >= 45;
    }

    private boolean isStretchMatch(JobMatch match) {
        return !isMainMatch(match) && score(match.getSemanticScore()) >= 20 && score(match.getFinalScore()) >= 15;
    }

    private int score(java.math.BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }
}
