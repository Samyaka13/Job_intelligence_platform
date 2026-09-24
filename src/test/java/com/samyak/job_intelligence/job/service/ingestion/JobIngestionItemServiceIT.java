package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.*;
import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementExtractionService;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
class JobIngestionItemServiceIT {

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
    private JobIngestionItemService jobIngestionItemService;

    @Autowired
    private JobService jobService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @MockitoBean
    private JobNormalizer jobNormalizer;

    @MockitoBean
    private JobSourceListingService jobSourceListingService;

    @MockitoBean
    private JobLocationService jobLocationService;

    @MockitoBean
    private JobRequirementExtractionService jobRequirementExtractionService;

    @BeforeEach
    void resetMocks() {

        reset(
                jobNormalizer,
                jobSourceListingService,
                jobLocationService,
                jobRequirementExtractionService
        );
    }

    @Test
    void shouldCreateNewJobAndExtractRequirements() {

        Company company =
                companyRepository.save(
                        new Company(
                                "airbnb",
                                "Airbnb",
                                "https://www.airbnb.com"
                        )
                );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

        when(rawJobListing.externalJobId())
                .thenReturn("greenhouse-1001");

        NormalizedJobData normalizedJobData =
                createNormalizedJobData(
                        "Java experience is required.",
                        "hash-1",
                        "fingerprint-1"
                );

        ExtractedJobRequirement javaRequirement =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Java experience is required.",
                        UUID.randomUUID().toString(),
                        RequirementMatchMode.SINGLE
                        );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName(),
                        "GREENHOUSE"
                )
        ).thenReturn(normalizedJobData);

        /*
         * First identity lookup:
         * source + externalJobId
         *
         * No existing source listing.
         */
        when(
                jobSourceListingService.findJobBySourceAndExternalJobId(
                        "GREENHOUSE",
                        "greenhouse-1001"
                )
        ).thenReturn(null);

        when(
                jobRequirementExtractionService.extract(
                        "Java experience is required."
                )
        ).thenReturn(
                List.of(javaRequirement)
        );

        boolean created =
                jobIngestionItemService.ingestListing(
                        company.getId(),
                        company.getDisplayName(),
                        "GREENHOUSE",
                        rawJobListing
                );

        assertThat(created)
                .isTrue();

        List<Job> jobs =
                jobRepository.findAllByCanonicalFingerprint(
                        "fingerprint-1"
                );

        assertThat(jobs)
                .hasSize(1);

        Job job = jobs.getFirst();

        assertThat(job.getDescription())
                .isEqualTo("Java experience is required.");

        assertThat(job.getDescriptionHash())
                .isEqualTo("hash-1");

        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(
                        job.getId()
                );

        assertThat(requirements)
                .hasSize(1);

        assertThat(requirements.getFirst().getValue())
                .isEqualTo("Java");

        verify(jobRequirementExtractionService)
                .extract("Java experience is required.");
    }

    @Test
    void shouldUpdateExistingJobWhenSourceAndExternalIdMatch() {

        Company company =
                companyRepository.save(
                        new Company(
                                "airbnb-source-match",
                                "Airbnb Source Match",
                                "https://www.airbnb.com"
                        )
                );

        Job existingJob =
                jobService.create(
                        company.getId(),
                        "Software Engineer",
                        "software engineer",
                        "Java experience is required.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.MID,
                        new BigDecimal("3.0"),
                        new BigDecimal("5.0"),
                        null,
                        null,
                        "USD",
                        Instant.parse("2026-09-15T00:00:00Z"),
                        null,
                        "https://airbnb.com/apply/123",
                        "fingerprint-source-match",
                        "hash-source-match"
                );

        JobRequirement existingRequirement =
                jobRequirementRepository.save(
                        new JobRequirement(
                                existingJob,
                                RequirementType.TECHNOLOGY,
                                "Java",
                                "java",
                                true,
                                null,
                                "Java experience is required.",
                                UUID.randomUUID().toString(),
                                RequirementMatchMode.SINGLE
                        )
                );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

        when(rawJobListing.externalJobId())
                .thenReturn("greenhouse-2001");

        NormalizedJobData normalizedJobData =
                createNormalizedJobData(
                        "Java experience is required.",
                        "hash-source-match",
                        "fingerprint-source-match"
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName(),
                        "GREENHOUSE"

                )
        ).thenReturn(normalizedJobData);

        /*
         * Primary identity lookup finds the existing Job.
         */
        when(
                jobSourceListingService.findJobBySourceAndExternalJobId(
                        "GREENHOUSE",
                        "greenhouse-2001"
                )
        ).thenReturn(existingJob);

        boolean created =
                jobIngestionItemService.ingestListing(
                        company.getId(),
                        company.getDisplayName(),
                        "GREENHOUSE",
                        rawJobListing
                );

        assertThat(created)
                .isFalse();

        Job updatedJob =
                jobRepository.findById(existingJob.getId())
                        .orElseThrow();

        assertThat(updatedJob.getId())
                .isEqualTo(existingJob.getId());

        assertThat(updatedJob.getDescription())
                .isEqualTo("Java experience is required.");

        assertThat(updatedJob.getDescriptionHash())
                .isEqualTo("hash-source-match");

        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(
                        existingJob.getId()
                );

        assertThat(requirements)
                .hasSize(1);

        assertThat(requirements.getFirst().getId())
                .isEqualTo(existingRequirement.getId());

        verifyNoInteractions(
                jobRequirementExtractionService
        );
    }

    @Test
    void shouldUseCanonicalFingerprintWhenSourceListingDoesNotExist() {

        Company company =
                companyRepository.save(
                        new Company(
                                "airbnb-fingerprint-fallback",
                                "Airbnb Fingerprint Fallback",
                                "https://www.airbnb.com"
                        )
                );

        Job existingJob =
                jobService.create(
                        company.getId(),
                        "Software Engineer",
                        "software engineer",
                        "Java experience is required.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.MID,
                        new BigDecimal("3.0"),
                        new BigDecimal("5.0"),
                        null,
                        null,
                        "USD",
                        Instant.parse("2026-09-15T00:00:00Z"),
                        null,
                        "https://airbnb.com/apply/456",
                        "fingerprint-fallback",
                        "hash-fallback"
                );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

        when(rawJobListing.externalJobId())
                .thenReturn("workday-3001");

        NormalizedJobData normalizedJobData =
                createNormalizedJobData(
                        "Java experience is required.",
                        "hash-fallback",
                        "fingerprint-fallback"
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName(),
                        "GREENHOUSE"

                )
        ).thenReturn(normalizedJobData);

        /*
         * Source listing doesn't exist.
         */
        when(
                jobSourceListingService.findJobBySourceAndExternalJobId(
                        "WORKDAY",
                        "workday-3001"
                )
        ).thenReturn(null);

        /*
         * Canonical fingerprint finds the existing Job.
         *
         * JobService internally resolves the fingerprint
         * through JobRepository.
         */
        boolean created =
                jobIngestionItemService.ingestListing(
                        company.getId(),
                        company.getDisplayName(),
                        "WORKDAY",
                        rawJobListing
                );

        assertThat(created)
                .isFalse();

        List<Job> jobs =
                jobRepository.findAllByCanonicalFingerprint(
                        "fingerprint-fallback"
                );

        assertThat(jobs)
                .hasSize(1);

        assertThat(jobs.getFirst().getId())
                .isEqualTo(existingJob.getId());

        /*
         * The new source listing should be associated with
         * the existing Job rather than creating a new Job.
         */
        verify(jobSourceListingService)
                .createOrRefresh(
                        existingJob.getId(),
                        "WORKDAY",
                        "workday-3001",
                        normalizedJobData.normalizedSourceUrl(),
                        rawJobListing.rawPayload(),
                        rawJobListing.postedAt()
                );
    }

    @Test
    void shouldUpdateDescriptionAndReplaceRequirementsWhenDescriptionChanges() {

        Company company =
                companyRepository.save(
                        new Company(
                                "airbnb-changed",
                                "Airbnb Changed",
                                "https://www.airbnb.com"
                        )
                );

        Job existingJob =
                jobService.create(
                        company.getId(),
                        "Software Engineer",
                        "software engineer",
                        "Java experience is required.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.MID,
                        new BigDecimal("3.0"),
                        new BigDecimal("5.0"),
                        null,
                        null,
                        "USD",
                        Instant.parse("2026-09-15T00:00:00Z"),
                        null,
                        "https://airbnb.com/apply/456",
                        "fingerprint-3",
                        "old-hash"
                );

        jobRequirementRepository.save(
                new JobRequirement(
                        existingJob,
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Java experience is required.",
                        UUID.randomUUID().toString(),
                        RequirementMatchMode.SINGLE
                )
        );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

        when(rawJobListing.externalJobId())
                .thenReturn("greenhouse-4001");

        NormalizedJobData normalizedJobData =
                createNormalizedJobData(
                        "Java and Spring Boot experience are required.",
                        "new-hash",
                        "fingerprint-3"
                );

        ExtractedJobRequirement springRequirement =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Spring Boot",
                        "spring boot",
                        true,
                        null,
                        "Spring Boot experience is required.",
                        UUID.randomUUID().toString(),
                        RequirementMatchMode.SINGLE
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName(),
                        "GREENHOUSE"

                )
        ).thenReturn(normalizedJobData);

        when(
                jobSourceListingService.findJobBySourceAndExternalJobId(
                        "GREENHOUSE",
                        "greenhouse-4001"
                )
        ).thenReturn(existingJob);

        when(
                jobRequirementExtractionService.extract(
                        "Java and Spring Boot experience are required."
                )
        ).thenReturn(
                List.of(springRequirement)
        );

        boolean created =
                jobIngestionItemService.ingestListing(
                        company.getId(),
                        company.getDisplayName(),
                        "GREENHOUSE",
                        rawJobListing
                );

        assertThat(created)
                .isFalse();

        Job job =
                jobRepository.findById(existingJob.getId())
                        .orElseThrow();

        assertThat(job.getDescription())
                .isEqualTo(
                        "Java and Spring Boot experience are required."
                );

        assertThat(job.getDescriptionHash())
                .isEqualTo("new-hash");

        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(
                        existingJob.getId()
                );

        assertThat(requirements)
                .hasSize(1);

        assertThat(requirements.getFirst().getValue())
                .isEqualTo("Spring Boot");

        verify(jobRequirementExtractionService)
                .extract(
                        "Java and Spring Boot experience are required."
                );
    }

    @Test
    void shouldUpdateNormalizedExperienceWhenSameSourceListingChanges() {

        Company company =
                companyRepository.save(
                        new Company(
                                "airbnb-experience-update",
                                "Airbnb Experience Update",
                                "https://www.airbnb.com"
                        )
                );

        Job existingJob =
                jobService.create(
                        company.getId(),
                        "Senior Community Growth Manager",
                        "senior community growth manager",
                        "8+ years experience in Sales, Business Operations, or Business Development.",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.SENIOR,
                        new BigDecimal("3.0"),
                        null,
                        null,
                        null,
                        "USD",
                        Instant.parse("2026-09-15T00:00:00Z"),
                        null,
                        "https://airbnb.com/apply/8178467",
                        "fingerprint-experience-old",
                        "same-hash"
                );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

        when(rawJobListing.externalJobId())
                .thenReturn("greenhouse-5001");

        NormalizedJobData normalizedJobData =
                new NormalizedJobData(
                        "Airbnb",
                        "airbnb",
                        "Senior Community Growth Manager",
                        "senior community growth manager",
                        "8+ years experience in Sales, Business Operations, or Business Development.",
                        "same-hash",
                        "https://airbnb.com/positions/8178467",
                        "https://airbnb.com/positions/8178467",
                        "https://airbnb.com/apply/8178467",
                        "https://airbnb.com/apply/8178467",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.SENIOR,
                        new BigDecimal("8.0"),
                        null,
                        null,
                        null,
                        "USD",
                        List.of(),
                        "fingerprint-experience-new",
                        Instant.parse("2026-09-15T00:00:00Z")
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName(),
                        "GREENHOUSE"

                )
        ).thenReturn(normalizedJobData);

        /*
         * This is the critical scenario:
         *
         * same source + external ID
         * but canonical fingerprint has changed
         * because experience changed.
         */
        when(
                jobSourceListingService.findJobBySourceAndExternalJobId(
                        "GREENHOUSE",
                        "greenhouse-5001"
                )
        ).thenReturn(existingJob);

        boolean created =
                jobIngestionItemService.ingestListing(
                        company.getId(),
                        company.getDisplayName(),
                        "GREENHOUSE",
                        rawJobListing
                );

        assertThat(created)
                .isFalse();

        Job updatedJob =
                jobRepository.findById(existingJob.getId())
                        .orElseThrow();

        assertThat(updatedJob.getId())
                .isEqualTo(existingJob.getId());

        assertThat(updatedJob.getExperienceMinYears())
                .isEqualByComparingTo("8.0");

        assertThat(updatedJob.getExperienceMaxYears())
                .isNull();

        assertThat(updatedJob.getDescriptionHash())
                .isEqualTo("same-hash");

        /*
         * The fingerprint should now represent the latest
         * normalized state of the job.
         */
        assertThat(updatedJob.getCanonicalFingerprint())
                .isEqualTo("fingerprint-experience-new");

        verifyNoInteractions(
                jobRequirementExtractionService
        );
    }

    private NormalizedJobData createNormalizedJobData(
            String description,
            String descriptionHash,
            String canonicalFingerprint
    ) {

        return new NormalizedJobData(
                "Airbnb",
                "airbnb",
                "Software Engineer",
                "software engineer",
                description,
                descriptionHash,
                "https://greenhouse.io/job/" + canonicalFingerprint,
                "https://greenhouse.io/job/" + canonicalFingerprint,
                "https://airbnb.com/apply/" + canonicalFingerprint,
                "https://airbnb.com/apply/" + canonicalFingerprint,
                EmploymentType.FULL_TIME,
                SeniorityLevel.MID,
                new BigDecimal("3.0"),
                new BigDecimal("5.0"),
                null,
                null,
                "USD",
                List.of(),
                canonicalFingerprint,
                Instant.parse("2026-09-15T00:00:00Z")
        );
    }
}