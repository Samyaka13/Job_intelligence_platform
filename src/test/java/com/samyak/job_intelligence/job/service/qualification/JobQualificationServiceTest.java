package com.samyak.job_intelligence.job.service.qualification;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.normalization.CandidateTextNormalizer;
import com.samyak.job_intelligence.candidate.service.CandidateEmploymentTypeService;
import com.samyak.job_intelligence.candidate.service.CandidateLocationService;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobLocation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobQualificationServiceTest {

    private final CandidateLocationService candidateLocationService = new CandidateLocationService(new CandidateTextNormalizer());
    private final CandidateEmploymentTypeService candidateEmploymentTypeService = new CandidateEmploymentTypeService();

    private NormalizedJobData createJob(
            BigDecimal experienceMin,
            BigDecimal experienceMax,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String salaryCurrency,
            List<NormalizedJobLocation> locations,
            EmploymentType employmentType
    ) {
        return new NormalizedJobData(
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
                employmentType,
                SeniorityLevel.MID,
                experienceMin,
                experienceMax,
                salaryMin,
                salaryMax,
                salaryCurrency,
                locations,
                null,
                null
        );
    }

    private CandidateProfile createCandidate(
            JsonNode preferredLocations,
            BigDecimal minimumSalary,
            String minimumSalaryCurrency,
            BigDecimal experienceYears,
            JsonNode preferredEmploymentTypes
    ) {
        return new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                preferredLocations,
                minimumSalary,
                minimumSalaryCurrency,
                true,
                experienceYears,
                preferredEmploymentTypes
        );
    }



    private final JobQualificationService service =
            new JobQualificationService(candidateLocationService,candidateEmploymentTypeService);


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
                null,
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
                new BigDecimal("2"),
                null
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
                null,
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
                new BigDecimal("2"),
                null
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
                null,
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
                new BigDecimal("2"),null
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
                null,
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
                null,
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
                null,
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
                new BigDecimal("2"),
                null
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
                null,
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
                new BigDecimal("2"),null
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
                null,
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
                new BigDecimal("2"),null
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
                null,
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
                new BigDecimal("2"),null
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
                null,
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
                new BigDecimal("2"),null
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }

    @Test
    void shouldQualifyWhenJobLocationMatchesCandidatePreference() {
        JsonNode preferredLocations = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                    [
                        "Bangalore"
                    ]
                    """);
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
                List.of(
                        new NormalizedJobLocation(
                                "bangalore",
                                "karnataka",
                                "india",
                                "bangalore karnataka india"
                        )
                ),
                null,
                null
        );
        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                preferredLocations,
                new BigDecimal("1200000"),
                "INR",
                true,
                new BigDecimal("2"),null
        );
        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }
    @Test
    void shouldRejectWhenNoJobLocationMatchesCandidatePreference() throws Exception {

        JsonNode preferredLocations = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                    [
                        "Bangalore"
                    ]
                    """);

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
                List.of(
                        new NormalizedJobLocation("mumbai","maharashtra","india","mumbai region")
                ),
                null,
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                preferredLocations,
                null,
                null,
                true,
                new BigDecimal("2"),null
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertFalse(result.qualified());
    }

    @Test
    void shouldQualifyWhenCandidateHasNoPreferredLocations() {

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
                List.of(
                        new NormalizedJobLocation(
                                "bangalore",
                                "karnataka",
                                "india",
                                "bangalore karnataka india"
                        )
                ),
                null,
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
                new BigDecimal("2"),null
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
    }

    @Test
    void shouldQualifyWhenAnyJobLocationMatchesCandidatePreference() throws Exception {

        JsonNode preferredLocations = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                [
                    "Bangalore"
                ]
                """);

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
                List.of(
                        new NormalizedJobLocation(
                                "mumbai",
                                "maharashtra",
                                "india",
                                "mumbai region"
                        ),
                        new NormalizedJobLocation(
                                "bangalore",
                                "karnataka",
                                "india",
                                "bangalore region"
                        )
                ),
                null,
                null
        );

        CandidateProfile candidate = new CandidateProfile(
                "Samyak",
                "samyak@example.com",
                "Bhopal",
                preferredLocations,
                null,
                null,
                true,
                new BigDecimal("2"),null
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }

    @Test
    void shouldReturnAllRejectionReasonsWhenMultipleRulesFail() throws Exception {

        JsonNode preferredLocations = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                [
                    "Bangalore"
                ]
                """);

        NormalizedJobData job = createJob(
                new BigDecimal("5"),
                new BigDecimal("7"),
                new BigDecimal("600000"),
                new BigDecimal("800000"),
                "INR",
                List.of(
                        new NormalizedJobLocation(
                                "mumbai",
                                "maharashtra",
                                "india",
                                "mumbai region"
                        )
                ),
                EmploymentType.FULL_TIME
        );

        CandidateProfile candidate = createCandidate(
                preferredLocations,
                new BigDecimal("1200000"),
                "INR",
                new BigDecimal("2"),
                null
        );

        JobQualificationResult result =
                service.qualify(job, candidate);

        assertFalse(result.qualified());

        assertEquals(3, result.rejectionReasons().size());

        assertTrue(result.rejectionReasons().contains(
                "Required experience exceeds candidate experience"
        ));

        assertTrue(result.rejectionReasons().contains(
                "Job maximum salary is below candidate minimum salary"
        ));

        assertTrue(result.rejectionReasons().contains(
                "Job location does not match candidate preferred locations"
        ));
    }

    @Test
    void shouldQualifyWhenJobEmploymentTypeMatchesPreference() throws Exception {

        JsonNode preferredTypes = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                ["FULL_TIME"]
                """);

        NormalizedJobData job = createJob(
                new BigDecimal("2"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                EmploymentType.FULL_TIME
        );

        CandidateProfile candidate = createCandidate(
                null,
                null,
                null,
                new BigDecimal("2"),
                preferredTypes
        );

        JobQualificationResult result = service.qualify(job, candidate);

        assertTrue(result.qualified());
        assertTrue(result.rejectionReasons().isEmpty());
    }

    @Test
    void shouldRejectWhenJobEmploymentTypeDoesNotMatchPreference() throws Exception {

        JsonNode preferredTypes = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                ["FULL_TIME"]
                """);

        NormalizedJobData job = createJob(
                new BigDecimal("2"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                EmploymentType.INTERNSHIP
        );

        CandidateProfile candidate = createCandidate(
                null,
                null,
                null,
                new BigDecimal("2"),
                preferredTypes
        );

        JobQualificationResult result = service.qualify(job, candidate);

        assertFalse(result.qualified());

        assertTrue(result.rejectionReasons().contains(
                "Job employment type does not match candidate preferences"
        ));
    }

    @Test
    void shouldQualifyWhenJobMatchesAnyPreferredEmploymentType() throws Exception {

        JsonNode preferredTypes = new tools.jackson.databind.ObjectMapper()
                .readTree("""
                ["FULL_TIME", "CONTRACT"]
                """);

        NormalizedJobData job = createJob(
                new BigDecimal("2"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                EmploymentType.CONTRACT
        );

        CandidateProfile candidate = createCandidate(
                null,
                null,
                null,
                new BigDecimal("2"),
                preferredTypes
        );

        JobQualificationResult result = service.qualify(job, candidate);

        assertTrue(result.qualified());
    }


    @Test
    void shouldQualifyWhenCandidateHasNoEmploymentTypePreference() {

        NormalizedJobData job = createJob(
                new BigDecimal("2"),
                new BigDecimal("5"),
                null,
                null,
                null,
                List.of(),
                EmploymentType.INTERNSHIP
        );
        CandidateProfile candidate = createCandidate(
                null,
                null,
                null,
                new BigDecimal("2"),
                null
        );

        JobQualificationResult result = service.qualify(job, candidate);

        assertTrue(result.qualified());
    }
}