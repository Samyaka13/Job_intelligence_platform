package com.samyak.job_intelligence;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.*;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.repository.JobMatchRepository;
import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import com.samyak.job_intelligence.job.service.matching.CandidateJobMatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class Phase1MatchingIntegrationTest {

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
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @Autowired
    private JobLocationRepository jobLocationRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private CandidateSkillRepository candidateSkillRepository;

    @Autowired
    private JobMatchRepository jobMatchRepository;

    @Autowired
    private CandidateJobMatchingService candidateJobMatchingService;

    @Test
    void shouldEvaluateRankAndPersistQualifiedJobMatch() {

        Company company = companyRepository.save(
                new Company(
                        "google",
                        "Google",
                        "https://google.com"
                )
        );

        Job job = new Job(
                company,
                "Backend Engineer",
                "backend engineer",
                "Backend engineering role requiring Java and Spring.",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                new BigDecimal("100000"),
                new BigDecimal("180000"),
                "INR",
                Instant.now(),
                null,
                "https://google.com/jobs/123",
                "fingerprint-123",
                "description-hash-123"
        );

        job.markSeen(Instant.now());
        job = jobRepository.save(job);

        jobLocationRepository.save(
                new JobLocation(
                        job,
                        "Bangalore",
                        "Karnataka",
                        "India",
                        RemoteType.UNKNOWN,
                        "Bangalore, Karnataka, India"
                )
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bangalore",
                JsonNodeFactory.instance.arrayNode().add("Bangalore"),
                new BigDecimal("100000"),
                "INR",
                true,
                new BigDecimal("3"),
                JsonNodeFactory.instance.arrayNode().add("FULL_TIME")
        );

        candidate = candidateProfileRepository.save(candidate);

        candidateSkillRepository.save(
                new CandidateSkill(
                        candidate,
                        "Java",
                        "java",
                        "INTERMEDIATE",
                        new BigDecimal("3.0"),
                        true
                )
        );

        candidateSkillRepository.save(
                new CandidateSkill(
                        candidate,
                        "Spring",
                        "spring",
                        "INTERMEDIATE",
                        new BigDecimal("3.0"),
                        true
                )
        );
        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        new BigDecimal("2.0"),
                        "Java is required"
                )
        );

        jobRequirementRepository.save(
                new JobRequirement(
                        job,
                        RequirementType.TECHNOLOGY,
                        "Spring",
                        "spring",
                        true,
                        new BigDecimal("2.0"),
                        "Spring is required"
                )
        );

        List<JobMatch> matches =
                candidateJobMatchingService.evaluateAndRank(
                        candidate.getId()
                );

        var persistedMatch =
                jobMatchRepository.findByJobIdAndCandidateProfileId(
                        job.getId(),
                        candidate.getId()
                );

        assertThat(persistedMatch.get().getMatchReasoning())
                .isNotBlank();

        System.out.println(
                "Qualification rejection: "
                        + persistedMatch.get().getMatchReasoning()
        );
        assertThat(persistedMatch)
                .isPresent();

        assertThat(persistedMatch.get().isHardQualified())
                .isTrue();

        assertThat(persistedMatch.get().getFinalScore())
                .isNotNull();

        assertThat(matches)
                .hasSize(1);

        JobMatch match = matches.getFirst();

        assertThat(match.getJob().getId())
                .isEqualTo(job.getId());

        assertThat(match.getCandidateProfile().getId())
                .isEqualTo(candidate.getId());

        assertThat(match.isHardQualified())
                .isTrue();

        assertThat(match.getFinalScore())
                .isNotNull();

        assertThat(
                jobMatchRepository
                        .findByJobIdAndCandidateProfileId(
                                job.getId(),
                                candidate.getId()
                        )
        )
                .isPresent();
    }
}
