package com.samyak.job_intelligence.job.service;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.service.CompanyService;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
                      EmploymentType employmentType,
                      SeniorityLevel seniorityLevel,
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
    public Job findByCanonicalFingerPrint(String canonicalFingerprint) {
        List<Job> jobs =
                jobRepository.findAllByCanonicalFingerprint(canonicalFingerprint);

        if (jobs.isEmpty()) {
            return null;
        }

        if (jobs.size() > 1) {
            throw new IllegalStateException(
                    "Multiple jobs found for canonical fingerprint: "
                            + canonicalFingerprint
            );
        }

        return jobs.getFirst();
    }
}
