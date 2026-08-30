package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.repository.JobSourceListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JobSourceListingServiceTest {

    @Mock
    private JobSourceListingRepository listingRepository;

    @Mock
    private JobSourceService jobSourceService;

    @Mock
    private JobService jobService;

    private JobSourceListingService listingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        listingService = new JobSourceListingService(
                listingRepository,
                jobSourceService,
                jobService
        );
    }

    @Test
    void shouldCreateListing() throws Exception {

        Job job = mock(Job.class);

        JobSource source = new JobSource(
                "GREENHOUSE",
                "Greenhouse",
                "ATS",
                "https://boards.greenhouse.io"
        );

        JsonNode payload = objectMapper.readTree(
                "{\"id\":\"12345\",\"title\":\"Backend Engineer\"}"
        );

        JobSourceListing savedListing = mock(JobSourceListing.class);

        when(jobService.getById(1L))
                .thenReturn(job);

        when(jobSourceService.getByCode("greenhouse"))
                .thenReturn(source);

        when(listingRepository.existsByJobSourceIdAndExternalJobId(
                any(),
                eq("12345")
        )).thenReturn(false);

        when(listingRepository.save(any(JobSourceListing.class)))
                .thenReturn(savedListing);

        JobSourceListing result = listingService.create(
                1L,
                "greenhouse",
                "12345",
                "https://boards.greenhouse.io/example/jobs/12345",
                payload,
                Instant.now()
        );

        assertThat(result).isSameAs(savedListing);

        verify(jobService).getById(1L);
        verify(jobSourceService).getByCode("greenhouse");
        verify(listingRepository)
                .existsByJobSourceIdAndExternalJobId(any(), eq("12345"));

        verify(listingRepository).save(any(JobSourceListing.class));
    }

    @Test
    void shouldRejectDuplicateListing() throws Exception {

        Job job = mock(Job.class);

        JobSource source = new JobSource(
                "GREENHOUSE",
                "Greenhouse",
                "ATS",
                "https://boards.greenhouse.io"
        );

        JsonNode payload = objectMapper.readTree(
                "{\"id\":\"12345\"}"
        );

        when(jobService.getById(1L))
                .thenReturn(job);

        when(jobSourceService.getByCode("GREENHOUSE"))
                .thenReturn(source);

        when(listingRepository.existsByJobSourceIdAndExternalJobId(
                any(),
                eq("12345")
        )).thenReturn(true);

        assertThatThrownBy(() ->
                listingService.create(
                        1L,
                        "GREENHOUSE",
                        "12345",
                        "https://boards.greenhouse.io/example/jobs/12345",
                        payload,
                        Instant.now()
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job source listing already exists");

        verify(listingRepository, never())
                .save(any(JobSourceListing.class));
    }

    @Test
    void shouldFindListingBySourceAndExternalJobId() {

        JobSource source = new JobSource(
                "GREENHOUSE",
                "Greenhouse",
                "ATS",
                "https://boards.greenhouse.io"
        );

        JobSourceListing listing = mock(JobSourceListing.class);

        when(jobSourceService.getByCode("greenhouse"))
                .thenReturn(source);

        when(listingRepository.findByJobSourceIdAndExternalJobId(
                any(),
                eq("12345")
        )).thenReturn(Optional.of(listing));

        JobSourceListing result =
                listingService.getBySourceAndExternalJobId(
                        "greenhouse",
                        "12345"
                );

        assertThat(result).isSameAs(listing);

        verify(jobSourceService).getByCode("greenhouse");
        verify(listingRepository)
                .findByJobSourceIdAndExternalJobId(any(), eq("12345"));
    }
}