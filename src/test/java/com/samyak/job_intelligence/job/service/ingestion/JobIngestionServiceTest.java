package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobIngestionServiceTest {

    @Mock
    private JobIngestionItemService jobIngestionItemService;

    @Mock
    private JobSourceListingService jobSourceListingService;

    @Mock
    private JobSourceCollector collector;

    @Mock
    private SourceConfiguration configuration;

    @Test
    void shouldCountCreatedAndUpdatedJobs() {

        Long companyId = 1L;
        String companyName = "Google";

        RawJobListing listing1 =
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

        RawJobListing listing2 =
                new RawJobListing(
                        "456",
                        "Java Engineer",
                        "Build Java services.",
                        "https://example.com/job/456",
                        "https://example.com/apply/456",
                        List.of(),
                        Instant.now(),
                        null
                );

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect(configuration))
                .thenReturn(List.of(listing1, listing2));

        when(jobIngestionItemService.ingestListing(
                eq(companyId),
                eq(companyName),
                eq("GREENHOUSE"),
                same(listing1)
        )).thenReturn(true);

        when(jobIngestionItemService.ingestListing(
                eq(companyId),
                eq(companyName),
                eq("GREENHOUSE"),
                same(listing2)
        )).thenReturn(false);

        when(jobSourceListingService.deactivateMissingListings(
                eq("GREENHOUSE"),
                anySet()
        )).thenReturn(0);

        JobIngestionService service =
                new JobIngestionService(
                        jobIngestionItemService,
                        jobSourceListingService
                );

        JobIngestionResult result =
                service.ingest(
                        companyId,
                        companyName,
                        collector,
                        configuration
                );

        assertThat(result.collected())
                .isEqualTo(2);

        assertThat(result.created())
                .isEqualTo(1);

        assertThat(result.updated())
                .isEqualTo(1);

        assertThat(result.failed())
                .isEqualTo(0);

        assertThat(result.deactivated())
                .isEqualTo(0);

        verify(jobIngestionItemService)
                .ingestListing(
                        companyId,
                        companyName,
                        "GREENHOUSE",
                        listing1
                );

        verify(jobIngestionItemService)
                .ingestListing(
                        companyId,
                        companyName,
                        "GREENHOUSE",
                        listing2
                );

        verify(jobSourceListingService)
                .deactivateMissingListings(
                        eq("GREENHOUSE"),
                        anySet()
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

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect(configuration))
                .thenReturn(
                        List.of(
                                listing1,
                                listing2,
                                listing3
                        )
                );

        // Listing 1 succeeds and creates a job
        when(jobIngestionItemService.ingestListing(
                eq(companyId),
                eq(companyName),
                eq("GREENHOUSE"),
                same(listing1)
        )).thenReturn(true);

        // Listing 2 fails
        when(jobIngestionItemService.ingestListing(
                eq(companyId),
                eq(companyName),
                eq("GREENHOUSE"),
                same(listing2)
        )).thenThrow(
                new RuntimeException("Normalization failed")
        );

        // Listing 3 succeeds and creates a job
        when(jobIngestionItemService.ingestListing(
                eq(companyId),
                eq(companyName),
                eq("GREENHOUSE"),
                same(listing3)
        )).thenReturn(true);

        when(jobSourceListingService.deactivateMissingListings(
                eq("GREENHOUSE"),
                anySet()
        )).thenReturn(0);

        JobIngestionService service =
                new JobIngestionService(
                        jobIngestionItemService,
                        jobSourceListingService
                );

        JobIngestionResult result =
                service.ingest(
                        companyId,
                        companyName,
                        collector,
                        configuration
                );

        assertThat(result.collected())
                .isEqualTo(3);

        assertThat(result.created())
                .isEqualTo(2);

        assertThat(result.updated())
                .isEqualTo(0);

        assertThat(result.failed())
                .isEqualTo(1);

        assertThat(result.deactivated())
                .isEqualTo(0);

        // All three listings were attempted
        verify(jobIngestionItemService)
                .ingestListing(
                        companyId,
                        companyName,
                        "GREENHOUSE",
                        listing1
                );

        verify(jobIngestionItemService)
                .ingestListing(
                        companyId,
                        companyName,
                        "GREENHOUSE",
                        listing2
                );

        verify(jobIngestionItemService)
                .ingestListing(
                        companyId,
                        companyName,
                        "GREENHOUSE",
                        listing3
                );

        // Deactivation happens once after the whole batch
        verify(jobSourceListingService)
                .deactivateMissingListings(
                        eq("GREENHOUSE"),
                        anySet()
                );
    }


    @Test
    void shouldCountUpdatedJobWhenItemServiceReturnsFalse() {

        Long companyId = 1L;
        String companyName = "Google";

        RawJobListing listing =
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

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect(configuration))
                .thenReturn(List.of(listing));

        when(jobIngestionItemService.ingestListing(
                eq(companyId),
                eq(companyName),
                eq("GREENHOUSE"),
                same(listing)
        )).thenReturn(false);

        when(jobSourceListingService.deactivateMissingListings(
                eq("GREENHOUSE"),
                anySet()
        )).thenReturn(0);

        JobIngestionService service =
                new JobIngestionService(
                        jobIngestionItemService,
                        jobSourceListingService
                );

        JobIngestionResult result =
                service.ingest(
                        companyId,
                        companyName,
                        collector,
                        configuration
                );

        assertThat(result.collected())
                .isEqualTo(1);

        assertThat(result.created())
                .isEqualTo(0);

        assertThat(result.updated())
                .isEqualTo(1);

        assertThat(result.failed())
                .isEqualTo(0);
    }
}