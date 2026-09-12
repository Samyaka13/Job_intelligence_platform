package com.samyak.job_intelligence.job.web.dto;

import com.samyak.job_intelligence.job.service.digest.DailyDigest;


import java.time.Instant;
import java.util.List;


public record DailyDigestResponse (Long candidateProfileId, Instant generatedAt, List<DailyDigestItem> jobs){
    public static DailyDigestResponse from(DailyDigest digest){
        return new DailyDigestResponse(digest.candidateProfileId(),digest.generatedAt(),digest.matches().stream().map(DailyDigestItem::from).toList());
    }
}
