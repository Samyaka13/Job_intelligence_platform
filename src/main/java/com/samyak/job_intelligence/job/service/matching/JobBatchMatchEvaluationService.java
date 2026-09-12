package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.domain.JobStatus;
import com.samyak.job_intelligence.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobBatchMatchEvaluationService {
    private final JobRepository jobRepository;
    private final JobMatchEvaluationService jobMatchEvaluationService;


    public JobBatchMatchEvaluationService(JobRepository jobRepository, JobMatchEvaluationService jobMatchEvaluationService) {
        this.jobRepository = jobRepository;
        this.jobMatchEvaluationService = jobMatchEvaluationService;
    }

    public List<JobMatch> evaluate(Long candidateProfileId){
        List<Job> jobs = jobRepository.findByStatusOrderByPostedAtDesc(JobStatus.ACTIVE);
        return jobs.stream().map(job -> jobMatchEvaluationService.evaluate(job.getId(),candidateProfileId)).toList();
    }
}
