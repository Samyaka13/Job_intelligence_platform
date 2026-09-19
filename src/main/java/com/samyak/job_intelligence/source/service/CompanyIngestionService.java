package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.service.CompanyService;
import com.samyak.job_intelligence.job.service.ingestion.JobIngestionResult;
import com.samyak.job_intelligence.job.service.ingestion.JobIngestionService;
import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyIngestionService {
    private final CompanyService companyService;
    private final SourceConfigurationService sourceConfigurationService;
    private final JobSourceCollectorResolver jobSourceCollectorResolver;
    private final JobIngestionService jobIngestionService;



    public CompanyIngestionService(CompanyService companyService, SourceConfigurationService sourceConfigurationService, JobSourceCollectorResolver jobSourceCollectorResolver, JobIngestionService jobIngestionService) {
        this.companyService = companyService;
        this.sourceConfigurationService = sourceConfigurationService;
        this.jobSourceCollectorResolver = jobSourceCollectorResolver;
        this.jobIngestionService = jobIngestionService;
    }

    public List<JobIngestionResult> ingest(Long companyId){
        Company company = companyService.getById(companyId);
        List<SourceConfiguration> configurations = sourceConfigurationService.getEnabledForCompany(companyId);

        return  configurations.stream().map(configuration -> {
            JobSourceCollector collector = jobSourceCollectorResolver.resolve(configuration);
            return jobIngestionService.ingest(
                    company.getId(),
                    company.getDisplayName(),
                    collector,
                    configuration
            );
        })
                .toList();
    }
}
