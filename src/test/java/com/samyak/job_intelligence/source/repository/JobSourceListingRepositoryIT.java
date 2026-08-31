package com.samyak.job_intelligence.source.repository;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobSourceListingRepositoryIT {

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    @Autowired
    private JobSourceListingRepository listingRepository;

    @Test
    void shouldPersistAndFindListingBySourceAndExternalJobId() throws Exception{

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
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.ENTRY,
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

        JobSource source = jobSourceRepository
                .findByCode("GREENHOUSE")
                .orElseThrow();

        JobSourceListing listing = listingRepository.saveAndFlush(
                new JobSourceListing(
                        job,
                        source,
                        "12345",
                        "https://boards.greenhouse.io/example/jobs/12345",
                        objectMapper.readTree("{\"id\":\"12345\",\"title\":\"Backend Engineer\"}"),
                        Instant.now()
                )
        );

        assertThat(listing.getId()).isNotNull();

        JobSourceListing retrieved =
                listingRepository
                        .findByJobSourceIdAndExternalJobId(
                                source.getId(),
                                "12345"
                        )
                        .orElseThrow();

        assertThat(retrieved.getId()).isEqualTo(listing.getId());
        assertThat(retrieved.getJob().getId()).isEqualTo(job.getId());
        assertThat(retrieved.getJobSource().getId()).isEqualTo(source.getId());
    }

    @Test
    void shouldRejectDuplicateExternalJobIdWithinSameSource() {

        Company company = companyRepository.save(
                new Company(
                        "example",
                        "Example Corp",
                        "https://example.com"
                )
        );

        Job job1 = jobRepository.save(
                createJob(
                        company,
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                )
        );

        Job job2 = jobRepository.save(
                createJob(
                        company,
                        "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
                )
        );

        JobSource source = jobSourceRepository
                .findByCode("GREENHOUSE")
                .orElseThrow();

        listingRepository.saveAndFlush(
                new JobSourceListing(
                        job1,
                        source,
                        "12345",
                        "https://boards.greenhouse.io/example/jobs/12345",
                        objectMapper.readTree("{\"id\":\"12345\"}"),
                        Instant.now()
                )
        );

        JobSourceListing duplicate = new JobSourceListing(
                job2,
                source,
                "12345",
                "https://boards.greenhouse.io/example/jobs/99999",
                objectMapper.readTree("{\"id\":\"12345\"}"),
                Instant.now()
        );

        assertThatThrownBy(() ->
                listingRepository.saveAndFlush(duplicate)
        ).isInstanceOf(Exception.class);
    }

    private Job createJob(Company company, String fingerprint) {
        return new Job(
                company,
                "Backend Engineer",
                "backend engineer",
                "Build backend services.",
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                BigDecimal.ZERO,
                BigDecimal.valueOf(2),
                null,
                null,
                null,
                Instant.now(),
                null,
                "https://example.com/jobs/" + fingerprint,
                fingerprint,
                null
        );
    }
}