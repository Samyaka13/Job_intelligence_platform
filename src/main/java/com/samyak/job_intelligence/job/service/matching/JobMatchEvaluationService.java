package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import com.samyak.job_intelligence.candidate.service.CandidateProfileService;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class JobMatchEvaluationService {
    private final JobService jobService;
    private final JobLocationRepository jobLocationRepository;
    private final CandidateProfileService candidateProfileService;
    private final JobMatchingService jobMatchingService;
    private final JobMatchPersistenceService jobMatchPersistenceService;

    public JobMatchEvaluationService(JobService jobService, JobLocationRepository jobLocationRepository, CandidateProfileService candidateProfileService,JobMatchingService jobMatchingService,JobMatchPersistenceService jobMatchPersistenceService) {
        this.jobService = jobService;
        this.jobLocationRepository = jobLocationRepository;
        this.candidateProfileService = candidateProfileService;
        this.jobMatchingService = jobMatchingService;
        this.jobMatchPersistenceService = jobMatchPersistenceService;
    }


    public JobMatch evaluate(Long jobId,Long candidateProfileId){
        Job job = jobService.getById(jobId);
        CandidateProfile candidateProfile = candidateProfileService.getById(candidateProfileId);
        List<JobLocation> jobLocation = jobLocationRepository.findByJobId(jobId);

        JobMatchingInput jobMatchingInput = new JobMatchingInput(job,candidateProfile,jobLocation);

        JobMatchResult jobMatchResult = jobMatchingService.match(jobMatchingInput);

        return jobMatchPersistenceService.save(jobId,candidateProfileId,jobMatchResult, Instant.now());


    }
}
