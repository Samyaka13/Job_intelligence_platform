package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.llm.JobDescriptionCleaner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobRequirementReprocessingService {

    private final JobService jobService;
    private final JobRequirementExtractionService jobRequirementExtractionService;
    private final JobRequirementService jobRequirementService;

    public JobRequirementReprocessingService(
            JobService jobService,
            JobRequirementExtractionService jobRequirementExtractionService,
            JobRequirementService jobRequirementService
    ) {
        this.jobService = jobService;
        this.jobRequirementExtractionService = jobRequirementExtractionService;
        this.jobRequirementService = jobRequirementService;
    }

    @Transactional
    public List<JobRequirement> reprocess(Long jobId) {

        Job job = jobService.getById(jobId);

        String cleanedDescription =
                JobDescriptionCleaner.clean(job.getDescription());

        List<ExtractedJobRequirement> extractedRequirements =
                jobRequirementExtractionService.extract(cleanedDescription);

        return jobRequirementService.replaceRequirements(
                jobId,
                extractedRequirements
        );
    }
}