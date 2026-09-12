package com.samyak.job_intelligence.job.service.digest;

import com.samyak.job_intelligence.job.domain.JobMatch;

import java.time.Instant;
import java.util.List;

public record DailyDigest(Long candidateProfileId, Instant generatedAt, List<JobMatch>  matches) {
}
