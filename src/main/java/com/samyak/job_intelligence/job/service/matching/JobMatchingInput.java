package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;

public record JobMatchingInput(
        NormalizedJobData job,
        CandidateProfile candidateProfile,
        Long jobId
) {
}