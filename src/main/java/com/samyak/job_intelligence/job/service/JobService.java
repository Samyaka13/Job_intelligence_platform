package com.samyak.job_intelligence.job.service;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.service.CompanyService;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyService companyService;

    public JobService(JobRepository jobRepository,CompanyService companyService){
        this.jobRepository = jobRepository;
        this.companyService = companyService;
    }

    @Transactional
    public Job create(Long companyId,
                      String title,
                      String normalizedTitle,
                      String description,
                      String employmentType,
                      String seniorityLevel,
                      BigDecimal experienceMinYears,
                      BigDecimal experienceMaxYears,
                      BigDecimal salaryMin,
                      BigDecimal salaryMax,
                      String salaryCurrency,
                      Instant postedAt,
                      Instant expiresAt,
                      String canonicalApplicationUrl,
                      String canonicalFingerprint,
                      String descriptionHash){
        Company company = companyService.getById(companyId);

        if(jobRepository.existsByCanonicalFingerprint(canonicalFingerprint)){
            throw new IllegalArgumentException(
                    "Job already exists for fingerprint: "
                            + canonicalFingerprint
            );
        }
        Job job = new Job(company,
                title,
                normalizedTitle,
                description,
                employmentType,
                seniorityLevel,
                experienceMinYears,
                experienceMaxYears,
                salaryMin,
                salaryMax,
                salaryCurrency,
                postedAt,
                expiresAt,
                canonicalApplicationUrl,
                canonicalFingerprint,
                descriptionHash
        );
        return jobRepository.save(job);
    }

    public Job getById(Long id){
       return jobRepository.findById(id).orElseThrow(() -> new IllegalArgumentException( "Job not found: " + id));
    }
}
