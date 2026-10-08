package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class RoleMatchingService {
    private final CandidateRoleProfileService candidateRoleProfileService;
    private final JobRoleClassifier jobRoleClassifier;

    public RoleMatchingService(CandidateRoleProfileService candidateRoleProfileService, JobRoleClassifier jobRoleClassifier) {
        this.candidateRoleProfileService = candidateRoleProfileService;
        this.jobRoleClassifier = jobRoleClassifier;
    }

    public RoleMatchResult match(Job job, CandidateProfile candidate) {
        JobRoleFamily family = jobRoleClassifier.classify(job);
        Map<JobRoleFamily, Integer> affinities = candidateRoleProfileService.affinities(candidate);
        int role = family == JobRoleFamily.UNKNOWN ? 50 : affinities.getOrDefault(family, 0);
        return new RoleMatchResult(family, BigDecimal.valueOf(role), BigDecimal.valueOf(seniorityScore(job.getSeniorityLevel(), candidate)));
    }

    private int seniorityScore(SeniorityLevel jobLevel, CandidateProfile candidate) {
        if (jobLevel == null || jobLevel == SeniorityLevel.UNKNOWN || candidate.getExperienceYears() == null) return 60;
        int candidateLevel = candidate.getExperienceYears().doubleValue() <= 1.5 ? 2 : candidate.getExperienceYears().doubleValue() <= 3 ? 3 : candidate.getExperienceYears().doubleValue() <= 6 ? 4 : 5;
        int required = switch (jobLevel) {
            case INTERN, ENTRY -> 1;
            case JUNIOR -> 2;
            case MID -> 3;
            case SENIOR -> 4;
            case LEAD -> 5;
            case STAFF -> 6;
            case UNKNOWN -> 3;
        };
        int difference = required - candidateLevel;
        return difference <= 0 ? 100 : difference == 1 ? 60 : difference == 2 ? 20 : 0;
    }
}
