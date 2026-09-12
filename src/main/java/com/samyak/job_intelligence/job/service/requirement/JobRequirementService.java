package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import com.samyak.job_intelligence.job.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobRequirementService {
    private final JobRequirementRepository jobRequirementRepository;
    private final JobService jobService;


    public JobRequirementService(JobRequirementRepository jobRequirementRepository, JobService jobService) {
        this.jobRequirementRepository = jobRequirementRepository;
        this.jobService = jobService;
    }

    public List<JobRequirement> replaceRequirements(Long jobId,List<ExtractedJobRequirement> extractedJobRequirements){
        Job job = jobService.getById(jobId);
        List<JobRequirement> existingRequirements = jobRequirementRepository.findByJobId(jobId);
        jobRequirementRepository.deleteAll(existingRequirements);
        if(extractedJobRequirements == null || extractedJobRequirements.isEmpty()) return List.of();

        List<JobRequirement> requirements = extractedJobRequirements.stream()
                .map(requirement -> new JobRequirement(
                        job,
                        requirement.requirementType(),
                        requirement.value(),
                        requirement.normalizedValue(),
                        requirement.mandatory(),
                        requirement.yearsRequired(),
                        requirement.requirementText()

                )).toList();
//        System.out.println("Requirements before save = " + requirements.size());
        System.out.println("Requirements job ID = " + job.getId());

        return jobRequirementRepository.saveAll(requirements);
    }
}
