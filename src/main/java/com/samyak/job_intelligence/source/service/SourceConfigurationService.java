package com.samyak.job_intelligence.source.service;


import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.service.CompanyService;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.repository.SourceConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Service

public class SourceConfigurationService {
    private final SourceConfigurationRepository sourceConfigurationRepository;
    private final CompanyService companyService;
    private final JobSourceService jobSourceService;

    public SourceConfigurationService(SourceConfigurationRepository sourceConfigurationRepository,CompanyService companyService,JobSourceService jobSourceService) {
        this.sourceConfigurationRepository = sourceConfigurationRepository;
        this.companyService = companyService;
        this.jobSourceService = jobSourceService;
    }

    @Transactional(readOnly = true)
    public List<SourceConfiguration> getEnabledForCompany(Long companyId) {
        return sourceConfigurationRepository
                .findByCompanyIdAndEnabledTrue(companyId);
    }

    @Transactional(readOnly = true)
    public SourceConfiguration getForCompanyAndSource(Long companyId, Long jobSourceId){
        return sourceConfigurationRepository
                .findByCompanyIdAndJobSourceId(companyId, jobSourceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Source configuration not found for company "
                                        + companyId
                                        + " and source "
                                        + jobSourceId
                        )
                );
    }

    @Transactional
    public SourceConfiguration create(Long companyId, String sourceCode, JsonNode configuration){
        Company company = companyService.getById(companyId);
        JobSource jobSource = jobSourceService.getByCode(sourceCode);

        sourceConfigurationRepository
                .findByCompanyIdAndJobSourceId(
                        companyId,
                        jobSource.getId()
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Source configuration already exists for company "
                                    + companyId
                                    + " and source "
                                    + jobSource.getCode()
                    );
                });
        SourceConfiguration sourceConfiguration =
                new SourceConfiguration(
                        company,
                        jobSource,
                        configuration,
                        true
                );
        return sourceConfigurationRepository.save(
                sourceConfiguration);
    }
}
