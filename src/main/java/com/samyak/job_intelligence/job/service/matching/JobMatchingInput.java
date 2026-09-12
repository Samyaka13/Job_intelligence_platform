package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;

import java.util.List;

public record JobMatchingInput(
        Job job,
        CandidateProfile candidateProfile,
        List<JobLocation> locations
) {
}