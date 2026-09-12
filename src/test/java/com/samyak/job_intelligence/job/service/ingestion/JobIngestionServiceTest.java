package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementExtractionService;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementService;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobIngestionServiceTest {

    @Mock
    private JobNormalizer jobNormalizer;

    @Mock
    private JobService jobService;

    @Mock
    private JobSourceListingService jobSourceListingService;

    @Mock
    private JobSourceCollector collector;

    @Mock
    private JobLocationService jobLocationService;

    @Mock
    private JobRequirementExtractionService jobRequirementExtractionService;

    @Mock
    private JobRequirementService jobRequirementService;

    @Test
    void shouldCreateJobWhenFingerprintDoesNotExist() {

        Long companyId = 1L;
        String companyName = "Google";

        RawJobListing rawJobListing =
                new RawJobListing(
                        "123",
                        "Backend Engineer",
                        "Build backend services.",
                        "https://example.com/job/123",
                        "https://example.com/apply/123",
                        List.of(),
                        Instant.now(),
                        null
                );

        NormalizedJobData normalizedJob =
                new NormalizedJobData(
                        companyName,
                        "google",
                        "Backend Engineer",
                        "backend engineer",
                        "Build backend services.",
                        "aaaaaaaa",
                        rawJobListing.sourceUrl(),
                        "https://example.com/job/123",
                        rawJobListing.applicationUrl(),
                        "https://example.com/apply/123",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.ENTRY,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(800000),
                        BigDecimal.valueOf(1200000),
                        "INR",
                        List.of(),
                        "fingerprint-123",
                        rawJobListing.postedAt()
                );

        Job job = mock(Job.class);

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect())
                .thenReturn(List.of(rawJobListing));

        when(jobNormalizer.normalize(rawJobListing, companyName))
                .thenReturn(normalizedJob);

        when(jobService.findByCanonicalFingerPrint("fingerprint-123"))
                .thenReturn(null);

        when(jobService.create(
                eq(companyId),
                eq("Backend Engineer"),
                eq("backend engineer"),
                eq("Build backend services."),
                eq(EmploymentType.FULL_TIME),
                eq(SeniorityLevel.ENTRY),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.valueOf(2)),
                eq(BigDecimal.valueOf(800000)),
                eq(BigDecimal.valueOf(1200000)),
                eq("INR"),
                eq(normalizedJob.postedAt()),
                isNull(),
                eq("https://example.com/apply/123"),
                eq("fingerprint-123"),
                eq("aaaaaaaa")
        )).thenReturn(job);

        when(jobSourceListingService.createOrRefresh(
                anyLong(),
                eq("GREENHOUSE"),
                eq("123"),
                eq("https://example.com/job/123"),
                isNull(),
                eq(rawJobListing.postedAt())
        )).thenReturn(mock(JobSourceListing.class));

        when(jobSourceListingService.deactivateMissingListings(
                eq("GREENHOUSE"),
                anySet()
        )).thenReturn(0);

        List<ExtractedJobRequirement> extractedRequirements =
                List.of(
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Java",
                                "java",
                                true,
                                null,
                                "Java is required."
                        ),
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Kafka",
                                "kafka",
                                false,
                                null,
                                "Kafka is a plus."
                        )
                );

        when(jobRequirementExtractionService.extract(
                normalizedJob.description()
        )).thenReturn(extractedRequirements);

        JobIngestionService service =
                new JobIngestionService(
                        jobNormalizer,
                        jobService,
                        jobSourceListingService,
                        jobLocationService,
                        jobRequirementExtractionService,
                        jobRequirementService
                );

        JobIngestionResult result =
                service.ingest(
                        companyId,
                        companyName,
                        collector
                );

        assertThat(result.collected())
                .isEqualTo(1);

        assertThat(result.created())
                .isEqualTo(1);

        assertThat(result.updated())
                .isEqualTo(0);

        assertThat(result.deactivated())
                .isEqualTo(0);

        verify(jobService).create(
                eq(companyId),
                eq("Backend Engineer"),
                eq("backend engineer"),
                eq("Build backend services."),
                eq(EmploymentType.FULL_TIME),
                eq(SeniorityLevel.ENTRY),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.valueOf(2)),
                eq(BigDecimal.valueOf(800000)),
                eq(BigDecimal.valueOf(1200000)),
                eq("INR"),
                eq(normalizedJob.postedAt()),
                isNull(),
                eq("https://example.com/apply/123"),
                eq("fingerprint-123"),
                eq("aaaaaaaa")
        );

        verify(jobSourceListingService).createOrRefresh(
                anyLong(),
                eq("GREENHOUSE"),
                eq("123"),
                eq("https://example.com/job/123"),
                isNull(),
                eq(rawJobListing.postedAt())
        );

        verify(jobSourceListingService)
                .deactivateMissingListings(
                        eq("GREENHOUSE"),
                        anySet()
                );

        verify(jobRequirementExtractionService).extract(
                normalizedJob.description()
        );

        verify(jobRequirementService).replaceRequirements(
                job.getId(),
                extractedRequirements
        );
    }

    @Test
    void shouldReuseExistingJobWhenFingerprintExists() {

        Long companyId = 1L;
        String companyName = "Google";

        RawJobListing rawJobListing =
                new RawJobListing(
                        "123",
                        "Backend Engineer",
                        "Build backend services.",
                        "https://example.com/job/123",
                        "https://example.com/apply/123",
                        List.of(),
                        Instant.now(),
                        null
                );

        NormalizedJobData normalizedJob =
                new NormalizedJobData(
                        companyName,
                        "google",
                        "Backend Engineer",
                        "backend engineer",
                        "Build backend services.",
                        "aaaaaaaa",
                        rawJobListing.sourceUrl(),
                        "https://example.com/job/123",
                        rawJobListing.applicationUrl(),
                        "https://example.com/apply/123",
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.ENTRY,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(800000),
                        BigDecimal.valueOf(1200000),
                        "INR",
                        List.of(),
                        "fingerprint-123",
                        rawJobListing.postedAt()
                );

        Job existingJob = mock(Job.class);

        when(existingJob.getId())
                .thenReturn(10L);

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect())
                .thenReturn(List.of(rawJobListing));

        when(jobNormalizer.normalize(rawJobListing, companyName))
                .thenReturn(normalizedJob);

        when(jobService.findByCanonicalFingerPrint("fingerprint-123"))
                .thenReturn(existingJob);

        when(jobSourceListingService.createOrRefresh(
                eq(10L),
                eq("GREENHOUSE"),
                eq("123"),
                eq("https://example.com/job/123"),
                isNull(),
                eq(rawJobListing.postedAt())
        )).thenReturn(mock(JobSourceListing.class));

        when(jobSourceListingService.deactivateMissingListings(
                eq("GREENHOUSE"),
                anySet()
        )).thenReturn(0);

        when(jobRequirementExtractionService.extract(
                normalizedJob.description()
        )).thenReturn(List.of());

        JobIngestionService service =
                new JobIngestionService(
                        jobNormalizer,
                        jobService,
                        jobSourceListingService,
                        jobLocationService,
                        jobRequirementExtractionService,
                        jobRequirementService
                );

        JobIngestionResult result =
                service.ingest(
                        companyId,
                        companyName,
                        collector
                );

        assertThat(result.collected())
                .isEqualTo(1);

        assertThat(result.created())
                .isEqualTo(0);

        assertThat(result.updated())
                .isEqualTo(1);

        assertThat(result.deactivated())
                .isEqualTo(0);

        verify(existingJob)
                .markSeen(any(Instant.class));

        verify(jobService, never()).create(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyString(),
                any(),
                any(),
                anyString(),
                anyString(),
                anyString()
        );

        verify(jobSourceListingService).createOrRefresh(
                eq(10L),
                eq("GREENHOUSE"),
                eq("123"),
                eq("https://example.com/job/123"),
                isNull(),
                eq(rawJobListing.postedAt())
        );

        verify(jobSourceListingService)
                .deactivateMissingListings(
                        eq("GREENHOUSE"),
                        anySet()
                );

        verify(jobLocationService).replaceLocations(
                10L,
                normalizedJob.locations()
        );

        verify(jobRequirementService).replaceRequirements(
                eq(10L),
                eq(List.of())
        );
    }
    @Test
    void shouldContinueProcessingWhenOneListingFails() {

        Long companyId = 1L;
        String companyName = "Google";

        RawJobListing listing1 =
                new RawJobListing(
                        "101",
                        "Backend Engineer",
                        "Build backend services.",
                        "https://example.com/job/101",
                        "https://example.com/apply/101",
                        List.of(),
                        Instant.now(),
                        null
                );

        RawJobListing listing2 =
                new RawJobListing(
                        "102",
                        "Java Engineer",
                        "Build Java services.",
                        "https://example.com/job/102",
                        "https://example.com/apply/102",
                        List.of(),
                        Instant.now(),
                        null
                );

        RawJobListing listing3 =
                new RawJobListing(
                        "103",
                        "Platform Engineer",
                        "Build platform services.",
                        "https://example.com/job/103",
                        "https://example.com/apply/103",
                        List.of(),
                        Instant.now(),
                        null
                );

        NormalizedJobData normalized1 =
                new NormalizedJobData(
                        companyName,
                        "google",
                        "Backend Engineer",
                        "backend engineer",
                        "Build backend services.",
                        "hash-101",
                        listing1.sourceUrl(),
                        listing1.sourceUrl(),
                        listing1.applicationUrl(),
                        listing1.applicationUrl(),
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.ENTRY,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(800000),
                        BigDecimal.valueOf(1200000),
                        "INR",
                        List.of(),
                        "fingerprint-101",
                        listing1.postedAt()
                );

        NormalizedJobData normalized3 =
                new NormalizedJobData(
                        companyName,
                        "google",
                        "Platform Engineer",
                        "platform engineer",
                        "Build platform services.",
                        "hash-103",
                        listing3.sourceUrl(),
                        listing3.sourceUrl(),
                        listing3.applicationUrl(),
                        listing3.applicationUrl(),
                        EmploymentType.FULL_TIME,
                        SeniorityLevel.ENTRY,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(800000),
                        BigDecimal.valueOf(1200000),
                        "INR",
                        List.of(),
                        "fingerprint-103",
                        listing3.postedAt()
                );

        Job job1 = mock(Job.class);
        Job job3 = mock(Job.class);

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect())
                .thenReturn(List.of(
                        listing1,
                        listing2,
                        listing3
                ));

        // Listing 1 succeeds
        when(jobNormalizer.normalize(listing1, companyName))
                .thenReturn(normalized1);

        // Listing 2 fails
        when(jobNormalizer.normalize(listing2, companyName))
                .thenThrow(new RuntimeException("Normalization failed"));

        // Listing 3 succeeds
        when(jobNormalizer.normalize(listing3, companyName))
                .thenReturn(normalized3);

        // Both successful listings are new jobs
        when(jobService.findByCanonicalFingerPrint("fingerprint-101"))
                .thenReturn(null);

        when(jobService.findByCanonicalFingerPrint("fingerprint-103"))
                .thenReturn(null);

        when(jobService.create(
                eq(companyId),
                eq("Backend Engineer"),
                eq("backend engineer"),
                eq("Build backend services."),
                eq(EmploymentType.FULL_TIME),
                eq(SeniorityLevel.ENTRY),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.valueOf(2)),
                eq(BigDecimal.valueOf(800000)),
                eq(BigDecimal.valueOf(1200000)),
                eq("INR"),
                eq(normalized1.postedAt()),
                isNull(),
                eq("https://example.com/apply/101"),
                eq("fingerprint-101"),
                eq("hash-101")
        )).thenReturn(job1);

        when(jobService.create(
                eq(companyId),
                eq("Platform Engineer"),
                eq("platform engineer"),
                eq("Build platform services."),
                eq(EmploymentType.FULL_TIME),
                eq(SeniorityLevel.ENTRY),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.valueOf(2)),
                eq(BigDecimal.valueOf(800000)),
                eq(BigDecimal.valueOf(1200000)),
                eq("INR"),
                eq(normalized3.postedAt()),
                isNull(),
                eq("https://example.com/apply/103"),
                eq("fingerprint-103"),
                eq("hash-103")
        )).thenReturn(job3);

        when(jobSourceListingService.createOrRefresh(
                anyLong(),
                eq("GREENHOUSE"),
                eq("101"),
                eq("https://example.com/job/101"),
                isNull(),
                eq(listing1.postedAt())
        )).thenReturn(mock(JobSourceListing.class));

        when(jobSourceListingService.createOrRefresh(
                anyLong(),
                eq("GREENHOUSE"),
                eq("103"),
                eq("https://example.com/job/103"),
                isNull(),
                eq(listing3.postedAt())
        )).thenReturn(mock(JobSourceListing.class));

        when(jobSourceListingService.deactivateMissingListings(
                eq("GREENHOUSE"),
                anySet()
        )).thenReturn(0);

        when(jobRequirementExtractionService.extract(anyString()))
                .thenReturn(List.of());

        JobIngestionService service =
                new JobIngestionService(
                        jobNormalizer,
                        jobService,
                        jobSourceListingService,
                        jobLocationService,
                        jobRequirementExtractionService,
                        jobRequirementService
                );

        JobIngestionResult result =
                service.ingest(
                        companyId,
                        companyName,
                        collector
                );

        // 3 listings were collected
        assertThat(result.collected())
                .isEqualTo(3);

        // Listing 1 + listing 3 succeeded
        assertThat(result.created())
                .isEqualTo(2);

        assertThat(result.updated())
                .isEqualTo(0);

        // Listing 2 failed
        assertThat(result.failed())
                .isEqualTo(1);

        assertThat(result.deactivated())
                .isEqualTo(0);

        // Listing 1 was processed
        verify(jobService).create(
                eq(companyId),
                eq("Backend Engineer"),
                eq("backend engineer"),
                eq("Build backend services."),
                eq(EmploymentType.FULL_TIME),
                eq(SeniorityLevel.ENTRY),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.valueOf(2)),
                eq(BigDecimal.valueOf(800000)),
                eq(BigDecimal.valueOf(1200000)),
                eq("INR"),
                eq(normalized1.postedAt()),
                isNull(),
                eq("https://example.com/apply/101"),
                eq("fingerprint-101"),
                eq("hash-101")
        );

        // Listing 3 was also processed despite listing 2 failing
        verify(jobService).create(
                eq(companyId),
                eq("Platform Engineer"),
                eq("platform engineer"),
                eq("Build platform services."),
                eq(EmploymentType.FULL_TIME),
                eq(SeniorityLevel.ENTRY),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.valueOf(2)),
                eq(BigDecimal.valueOf(800000)),
                eq(BigDecimal.valueOf(1200000)),
                eq("INR"),
                eq(normalized3.postedAt()),
                isNull(),
                eq("https://example.com/apply/103"),
                eq("fingerprint-103"),
                eq("hash-103")
        );

        // Failed listing must not continue into persistence
        verify(jobService, never())
                .findByCanonicalFingerPrint("fingerprint-102");

        // Deactivation happens once after the whole batch
        verify(jobSourceListingService)
                .deactivateMissingListings(
                        eq("GREENHOUSE"),
                        anySet()
                );
    }
}