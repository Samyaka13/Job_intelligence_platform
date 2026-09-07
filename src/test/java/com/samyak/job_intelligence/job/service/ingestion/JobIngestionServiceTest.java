package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
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

        when(collector.getSource()).thenReturn("GREENHOUSE");
        when(collector.collect()).thenReturn(List.of(rawJobListing));

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

        JobIngestionService service =
                new JobIngestionService(
                        jobNormalizer,
                        jobService,
                        jobSourceListingService
                );

        service.ingest(companyId, companyName, collector);

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

        when(existingJob.getId()).thenReturn(10L);

        when(collector.getSource()).thenReturn("GREENHOUSE");
        when(collector.collect()).thenReturn(List.of(rawJobListing));

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

        JobIngestionService service =
                new JobIngestionService(
                        jobNormalizer,
                        jobService,
                        jobSourceListingService
                );

        service.ingest(companyId, companyName, collector);

        verify(existingJob).markSeen(any(Instant.class));

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
    }
}