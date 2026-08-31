package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.repository.JobSourceRepository;
import com.samyak.job_intelligence.source.repository.JobSourceListingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class JobSourceListingServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
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
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    @Autowired
    private JobSourceListingRepository listingRepository;

    @Autowired
    private JobSourceListingService listingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateListingThroughService() throws Exception {

        Company company = companyRepository.save(
                new Company(
                        "example",
                        "Example Corp",
                        "https://example.com"
                )
        );

        Job job = jobRepository.save(
                new Job(
                        company,
                        "Backend Engineer",
                        "backend engineer",
                        "Build backend services using Java and Spring Boot.",
                        "FULL_TIME",
                        "ENTRY",
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2),
                        null,
                        null,
                        null,
                        Instant.now(),
                        null,
                        "https://example.com/jobs/123",
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                )
        );

      JobSource source =  jobSourceRepository.save(
                new JobSource(
                        "GREENHOUSE",
                        "Greenhouse",
                        "ATS",
                        "https://boards.greenhouse.io"
                )
        );

        JobSourceListing listing = listingService.createOrRefresh(
                job.getId(),
                "greenhouse",
                "12345",
                "https://boards.greenhouse.io/example/jobs/12345",
                objectMapper.readTree(
                        "{\"id\":\"12345\",\"title\":\"Backend Engineer\"}"
                ),
                Instant.now()
        );

        assertThat(listing.getId()).isNotNull();

        JobSourceListing persisted =
                listingRepository
                        .findByJobSourceIdAndExternalJobId(
                                source.getId(),
                                "12345"
                        )
                        .orElseThrow();

        assertThat(persisted.getId()).isEqualTo(listing.getId());
        assertThat(persisted.getJob().getId()).isEqualTo(job.getId());
        assertThat(persisted.getJobSource().getId())
                .isEqualTo(source.getId());
        assertThat(
                jobSourceRepository.findById(source.getId())
                        .orElseThrow()
                        .getCode()
        ).isEqualTo("GREENHOUSE");
    }
}