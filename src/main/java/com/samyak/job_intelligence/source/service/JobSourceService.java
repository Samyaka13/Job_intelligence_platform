package com.samyak.job_intelligence.source.service;


import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.repository.JobSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobSourceService {
    private final JobSourceRepository jobSourceRepository;

    public JobSourceService(JobSourceRepository jobSourceRepository){
        this.jobSourceRepository = jobSourceRepository;
    }

    public JobSource getByCode(String code){
        String normalisedCode = code.toUpperCase().trim();
        return jobSourceRepository.findByCode(normalisedCode).orElseThrow(() -> new IllegalArgumentException("Job source not found: " + normalisedCode));

    }
}
