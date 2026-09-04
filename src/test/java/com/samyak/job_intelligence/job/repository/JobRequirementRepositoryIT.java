package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Transactional
@Import(TestcontainersConfiguration.class)
class JobRequirementRepositoryIT {

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
    private JobRequirementRepository jobRequirementRepository;

    @Test
    void shouldFindRequirementsByJobId() {

        Company company = companyRepository.save(
                new Company(
                        "example company",
                        "Example Company",
                        "https://example.com"
                )
        );

        Job job = jobRepository.save(
                new Job(
                        company,
                        "Software Engineer",
                        "software engineer",
                        "Backend engineering role.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.MID,
                        new BigDecimal("2"),
                        new BigDecimal("5"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        "https://example.com/jobs/123",
                        "fingerprint-job-123",
                        "description-hash-123"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Experience with Java"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.TECHNOLOGY,
                        "Spring Boot",
                        "spring boot",
                        true,
                        null,
                        "Experience with Spring Boot"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.EDUCATION,
                        "Bachelor's Degree",
                        "bachelor's degree",
                        false,
                        null,
                        "Bachelor's degree preferred"
                )
        );

        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(job.getId());

        assertEquals(3, requirements.size());
    }

    @Test
    void shouldFindRequirementsByJobIdAndRequirementType() {

        Company company = companyRepository.save(
                new Company(
                        "another company",
                        "Another Company",
                        "https://another-example.com"
                )
        );

        Job job = jobRepository.save(
                new Job(
                        company,
                        "Backend Engineer",
                        "backend engineer",
                        "Backend engineering role.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.MID,
                        new BigDecimal("2"),
                        new BigDecimal("5"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        "https://another-example.com/jobs/123",
                        "fingerprint-job-456",
                        "description-hash-456"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Experience with Java"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.TECHNOLOGY,
                        "Kafka",
                        "kafka",
                        false,
                        null,
                        "Knowledge of Kafka"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.EDUCATION,
                        "Bachelor's Degree",
                        "bachelor's degree",
                        false,
                        null,
                        "Bachelor's degree preferred"
                )
        );

        List<JobRequirement> skillRequirements =
                jobRequirementRepository.findByJobIdAndRequirementType(
                        job.getId(),
                        RequirementType.TECHNOLOGY
                );

        List<JobRequirement> educationRequirements =
                jobRequirementRepository.findByJobIdAndRequirementType(
                        job.getId(),
                        RequirementType.EDUCATION
                );

        assertEquals(2, skillRequirements.size());
        assertEquals(1, educationRequirements.size());
    }
}