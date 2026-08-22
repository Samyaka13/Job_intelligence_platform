package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.Job;
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
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldPersistAndRetrieveJobByFingerprint() {

        Company company = companyRepository.save(
                new Company(
                        "google",
                        "Google",
                        "https://www.google.com"
                )
        );

        Job job = new Job(
                company,
                "Backend Engineer",
                "backend engineer",
                "Build backend services using Java and Spring Boot.",
                "FULL_TIME",
                "ENTRY",
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

        Job savedJob = jobRepository.saveAndFlush(job);

        assertThat(savedJob.getId()).isNotNull();

        Job retrieved = jobRepository
                .findByCanonicalFingerprint(savedJob.getCanonicalFingerprint())
                .orElseThrow();

        assertThat(retrieved.getId()).isEqualTo(savedJob.getId());
        assertThat(retrieved.getTitle()).isEqualTo("Backend Engineer");
        assertThat(retrieved.getCompany().getCanonicalName())
                .isEqualTo("google");
    }
}