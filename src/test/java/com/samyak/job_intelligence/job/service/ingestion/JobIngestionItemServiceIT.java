package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
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
                        "Java experience is required."
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName()
                )
        ).thenReturn(normalizedJobData);



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

        Job job =
                jobRepository
                        .findByCanonicalFingerprint("fingerprint-1")
                        .orElseThrow();

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
    void shouldNotExtractRequirementsWhenDescriptionHasNotChanged() {

        Company company =
                companyRepository.save(
                        new Company(
                                "airbnb-unchanged",
                                "Airbnb Unchanged",
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
                        "fingerprint-2",
                        "hash-2"
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
                                "Java experience is required."
                        )
                );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

        NormalizedJobData normalizedJobData =
                createNormalizedJobData(
                        "Java experience is required.",
                        "hash-2",
                        "fingerprint-2"
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName()
                )
        ).thenReturn(normalizedJobData);

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
                jobRepository
                        .findById(existingJob.getId())
                        .orElseThrow();

        assertThat(job.getDescription())
                .isEqualTo("Java experience is required.");

        assertThat(job.getDescriptionHash())
                .isEqualTo("hash-2");

        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(
                        existingJob.getId()
                );

        assertThat(requirements)
                .hasSize(1);

        assertThat(requirements.getFirst().getId())
                .isEqualTo(existingRequirement.getId());

        assertThat(requirements.getFirst().getValue())
                .isEqualTo("Java");

        verifyNoInteractions(
                jobRequirementExtractionService
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
                        "Java experience is required."
                )
        );

        RawJobListing rawJobListing =
                mock(RawJobListing.class);

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
                        "Spring Boot experience is required."
                );

        when(
                jobNormalizer.normalize(
                        rawJobListing,
                        company.getDisplayName()
                )
        ).thenReturn(normalizedJobData);

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
                jobRepository
                        .findById(existingJob.getId())
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
