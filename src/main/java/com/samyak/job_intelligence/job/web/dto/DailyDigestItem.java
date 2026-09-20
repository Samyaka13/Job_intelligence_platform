package com.samyak.job_intelligence.job.web.dto;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobMatch;

import java.math.BigDecimal;
import java.util.List;

public record DailyDigestItem(Long jobId,
                              String title,
                              String companyName,
                              BigDecimal finalScore,
                              String matchReasoning,
                              List<String> mandatorySkills,
                              List<String> missingMandatorySkills,
                              String applicationUrl) {
    public static DailyDigestItem from(JobMatch match){
        Job job = match.getJob();
        return new DailyDigestItem(
                job.getId(),
                job.getTitle(),
                job.getCompany().getDisplayName(),
                match.getFinalScore(),
                match.getMatchReasoning(),
                match.getMandatorySkills(),
                match.getMissingMandatorySkills(),
                job.getCanonicalApplicationUrl()
        );
    }
}
