package com.samyak.job_intelligence.job.web.dto;

import com.samyak.job_intelligence.job.service.digest.DailyDigest;


import java.time.Instant;
import java.util.List;


public record DailyDigestResponse (
        Long candidateProfileId,
        Instant generatedAt,
        List<DailyDigestItem> jobs,
        List<DailyDigestItem> stretchJobs){
    public DailyDigestResponse(Long candidateProfileId, Instant generatedAt, List<DailyDigestItem> jobs) {
        this(candidateProfileId, generatedAt, jobs, List.of());
    }
    public static DailyDigestResponse from(DailyDigest digest){
        return new DailyDigestResponse(digest.candidateProfileId(),
                digest.generatedAt(),
                digest.matches().stream().map(DailyDigestItem::from).toList(),
                digest.stretchMatches().stream().map(DailyDigestItem::from).toList());
    }
}
