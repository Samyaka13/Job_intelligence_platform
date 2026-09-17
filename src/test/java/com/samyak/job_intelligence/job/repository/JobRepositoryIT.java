package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobRepositoryIT {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("job_intelligence_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldPersistAndRetrieveJobByFingerprint() {

        Company company =
                companyRepository.save(
                        new Company(
                                "google",
                                "Google",
                                "https://www.google.com"
                        )
                );

        Job job =
                new Job(
                        company,
                        "Backend Engineer",
                        "backend engineer",
                        "Build backend services using Java and Spring Boot.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.ENTRY,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(800000),
                        BigDecimal.valueOf(1200000),
                        "INR",
                        Instant.now(),
                        null,
                        "https://example.com/jobs/123",
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                );

        Job savedJob =
                jobRepository.saveAndFlush(job);

        assertThat(savedJob.getId())
                .isNotNull();

        List<Job> retrievedJobs =
                jobRepository.findAllByCanonicalFingerprint(
                        savedJob.getCanonicalFingerprint()
                );

        assertThat(retrievedJobs)
                .hasSize(1);

        Job retrieved =
                retrievedJobs.getFirst();

        assertThat(retrieved.getId())
                .isEqualTo(savedJob.getId());

        assertThat(retrieved.getTitle())
                .isEqualTo("Backend Engineer");

        assertThat(retrieved.getCompany().getCanonicalName())
                .isEqualTo("google");
    }

    @Test
    void shouldAllowMultipleJobsWithSameCanonicalFingerprint() {

        Company company =
                companyRepository.save(
                        new Company(
                                "google-duplicate-fingerprint",
                                "Google Duplicate Fingerprint",
                                "https://www.google.com"
                        )
                );

        String fingerprint =
                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc";

        Job firstJob =
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
                        fingerprint,
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
                );

        Job secondJob =
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
                        "https://example.com/jobs/456",
                        fingerprint,
                        "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
                );

        Job savedFirstJob =
                jobRepository.saveAndFlush(firstJob);

        Job savedSecondJob =
                jobRepository.saveAndFlush(secondJob);

        assertThat(savedFirstJob.getId())
                .isNotEqualTo(savedSecondJob.getId());

        List<Job> jobs =
                jobRepository.findAllByCanonicalFingerprint(
                        fingerprint
                );

        assertThat(jobs)
                .hasSize(2);

        assertThat(jobs)
                .extracting(Job::getId)
                .containsExactlyInAnyOrder(
                        savedFirstJob.getId(),
                        savedSecondJob.getId()
                );
    }
}