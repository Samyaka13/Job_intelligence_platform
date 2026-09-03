package com.samyak.job_intelligence.job.service.qualification;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobQualificationServiceTest {

    private final JobQualificationService service =
            new JobQualificationService();

    @Test
    void shouldRejectWhenRequiredExperienceExceedsCandidateExperience() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Senior Software Engineer",
                "senior software engineer",
                "Backend engineering role requiring 3 years of experience.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.SENIOR,
                new BigDecimal("3"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                (JsonNode) null,
                null,
                null,
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertFalse(result.qualified());
        assertEquals(
                1,
                result.rejectionReasons().size()
        );
        assertEquals(
                "Required experience exceeds candidate experience",
                result.rejectionReasons().getFirst()
        );
    }

    @Test
    void shouldQualifyWhenCandidateExperienceMatchesMinimumRequirement() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Backend engineering role requiring 2 years of experience.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                null,
                null,
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }
    @Test
    void shouldQualifyWhenJobHasNoExperienceRequirement() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Software engineering role.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                null,
                null,
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }
    @Test
    void shouldQualifyWhenCandidateExperienceIsUnknown() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Senior Software Engineer",
                "senior software engineer",
                "Backend engineering role requiring 3 years of experience.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.SENIOR,
                new BigDecimal("3"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                null,
                null,
                true,
                null
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }

    @Test
    void shouldRejectWhenJobMaximumSalaryIsBelowCandidateMinimumSalary() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Backend engineering role.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                new BigDecimal("800000"),
                new BigDecimal("1000000"),
                "INR",
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                new BigDecimal("1200000"),
                "INR",
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertFalse(result.qualified());

        assertEquals(1, result.rejectionReasons().size());

        assertEquals(
                "Job maximum salary is below candidate minimum salary",
                result.rejectionReasons().getFirst()
        );
    }

    @Test
    void shouldQualifyWhenJobMaximumSalaryMatchesCandidateMinimumSalary() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Backend engineering role.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                new BigDecimal("800000"),
                new BigDecimal("1200000"),
                "INR",
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                new BigDecimal("1200000"),
                "INR",
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }

    @Test
    void shouldQualifyWhenJobSalaryIsUnknown() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Backend engineering role.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                new BigDecimal("1200000"),
                "INR",
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }

    @Test
    void shouldQualifyWhenCandidateMinimumSalaryIsUnknown() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Backend engineering role.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                new BigDecimal("800000"),
                new BigDecimal("1000000"),
                "INR",
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                null,
                null,
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }


    @Test
    void shouldQualifyWhenSalaryCurrenciesDoNotMatch() {

        NormalizedJobData job = new NormalizedJobData(
                "Example Company",
                "example company",
                "Software Engineer",
                "software engineer",
                "Backend engineering role.",
                "description-hash",
                "https://example.com/job/123",
                "https://example.com/job/123",
                "https://example.com/apply/123",
                "https://example.com/apply/123",
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("2"),
                new BigDecimal("5"),
                new BigDecimal("80000"),
                new BigDecimal("100000"),
                "USD",
                List.of(),
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                null,
                new BigDecimal("1200000"),
                "INR",
                true,
                new BigDecimal("2")
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }
}