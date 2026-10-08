package com.samyak.job_intelligence.job.service.digest;

import com.samyak.job_intelligence.job.domain.JobMatch;

import java.time.Instant;
import java.util.List;

public record DailyDigest(
        Long candidateProfileId,
        Instant generatedAt,
        List<JobMatch> matches,
        List<JobMatch> stretchMatches
) {
    public DailyDigest(Long candidateProfileId, Instant generatedAt, List<JobMatch> matches) {
        this(candidateProfileId, generatedAt, matches, List.of());
    }
}
