package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.service.ingestion.JobIngestionResult;
import com.samyak.job_intelligence.job.service.ingestion.JobIngestionService;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceType;
import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.greehouse.GreenhouseCollector;
import com.samyak.job_intelligence.source.repository.JobSourceRepository;
import com.samyak.job_intelligence.source.repository.SourceConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
class CompanyIngestionServiceIT {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("job_intelligence_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CompanyIngestionService companyIngestionService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    @Autowired
    private SourceConfigurationRepository sourceConfigurationRepository;

    @MockitoBean
    private GreenhouseCollector greenhouseCollector;

    @MockitoBean
    private JobIngestionService jobIngestionService;

    @Test
    void shouldResolveEnabledGreenhouseConfigurationAndDelegateIngestion() {

        Company company = companyRepository.save(
                new Company(
                        "airbnb",
                        "Airbnb",
                        "https://www.airbnb.com"
                )
        );

        JobSource greenhouse =
                jobSourceRepository.findByCode("GREENHOUSE")
                        .orElseThrow();

        SourceConfiguration sourceConfiguration =
                sourceConfigurationRepository.save(
                        new SourceConfiguration(
                                company,
                                greenhouse,
                                JsonNodeFactory.instance
                                        .objectNode()
                                        .put("boardToken", "airbnb"),
                                true
                        )
                );

        when(jobIngestionService.ingest(
                eq(company.getId()),
                eq(company.getDisplayName()),
                eq(greenhouseCollector),
                argThat(configuration ->
                        configuration.getId().equals(sourceConfiguration.getId())
                )
        )).thenReturn(
                new JobIngestionResult(
                        10,
                        8,
                        2,
                        0,
                        0
                )
        );

        List<JobIngestionResult> results =
                companyIngestionService.ingest(company.getId());

        assertThat(results)
                .hasSize(1);

        assertThat(results.getFirst().collected())
                .isEqualTo(10);

        assertThat(results.getFirst().created())
                .isEqualTo(8);

        assertThat(results.getFirst().updated())
                .isEqualTo(2);

        verify(jobIngestionService)
                .ingest(
                        eq(company.getId()),
                        eq(company.getDisplayName()),
                        eq(greenhouseCollector),
                        argThat(configuration ->
                                configuration.getId().equals(sourceConfiguration.getId())
                        )
                );

        verifyNoInteractions(greenhouseCollector);
    }

    @Test
    void shouldIgnoreDisabledSourceConfigurations() {

        Company company = companyRepository.save(
                new Company(
                        "airbnb-disabled-test",
                        "Airbnb Disabled Test",
                        "https://www.airbnb.com"
                )
        );

        JobSource greenhouse =
                jobSourceRepository.findByCode("GREENHOUSE")
                        .orElseThrow();

        sourceConfigurationRepository.save(
                new SourceConfiguration(
                        company,
                        greenhouse,
                        JsonNodeFactory.instance
                                .objectNode()
                                .put("boardToken", "airbnb"),
                        false
                )
        );

        List<JobIngestionResult> results =
                companyIngestionService.ingest(company.getId());

        assertThat(results)
                .isEmpty();

        verifyNoInteractions(jobIngestionService);
        verifyNoInteractions(greenhouseCollector);
    }
}