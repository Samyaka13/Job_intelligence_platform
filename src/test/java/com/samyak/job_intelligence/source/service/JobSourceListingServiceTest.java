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

        // Arrange
        Job job = mock(Job.class);

        JobSource source = mock(JobSource.class);
        when(source.getId()).thenReturn(1L);
        when(source.getCode()).thenReturn("GREENHOUSE");

        JsonNode payload = objectMapper.readTree(
                "{\"id\":\"12345\",\"title\":\"Backend Engineer\"}"
        );

        JobSourceListing savedListing = mock(JobSourceListing.class);

        when(jobService.getById(1L))
                .thenReturn(job);

        when(jobSourceService.getByCode("greenhouse"))
                .thenReturn(source);

        // No existing listing -> create a new one
        when(listingRepository.findByJobSourceIdAndExternalJobId(
                1L,
                "12345"
        )).thenReturn(Optional.empty());

        when(listingRepository.save(any(JobSourceListing.class)))
                .thenReturn(savedListing);

        // Act
        JobSourceListing result = listingService.createOrRefresh(
                1L,
                "greenhouse",
                "12345",
                "https://boards.greenhouse.io/example/jobs/12345",
                payload,
                Instant.now()
        );

        // Assert
        assertThat(result).isSameAs(savedListing);

        verify(jobService).getById(1L);

        verify(jobSourceService)
                .getByCode("greenhouse");

        verify(listingRepository)
                .findByJobSourceIdAndExternalJobId(
                        1L,
                        "12345"
                );

        verify(listingRepository)
                .save(any(JobSourceListing.class));
    }

    @Test
    void shouldRefreshExistingListing() throws Exception {

        // Arrange
        Job job = mock(Job.class);

        JobSource source = mock(JobSource.class);
        when(source.getId()).thenReturn(1L);
        when(source.getCode()).thenReturn("GREENHOUSE");

        JsonNode payload = objectMapper.readTree(
                "{\"id\":\"12345\"}"
        );

        JobSourceListing existingListing =
                mock(JobSourceListing.class);

        when(jobService.getById(1L))
                .thenReturn(job);

        when(jobSourceService.getByCode("GREENHOUSE"))
                .thenReturn(source);

        // Existing listing found -> refresh it
        when(listingRepository.findByJobSourceIdAndExternalJobId(
                1L,
                "12345"
        )).thenReturn(Optional.of(existingListing));

        Instant sourcePostedAt = Instant.now();

        // Act
        JobSourceListing result = listingService.createOrRefresh(
                1L,
                "GREENHOUSE",
                "12345",
                "https://boards.greenhouse.io/example/jobs/12345",
                payload,
                sourcePostedAt
        );

        // Assert
        assertThat(result).isSameAs(existingListing);

        verify(jobService)
                .getById(1L);

        verify(jobSourceService)
                .getByCode("GREENHOUSE");

        verify(listingRepository)
                .findByJobSourceIdAndExternalJobId(
                        1L,
                        "12345"
                );

        verify(existingListing)
                .refresh(
                        "https://boards.greenhouse.io/example/jobs/12345",
                        payload,
                        sourcePostedAt
                );

        // Should NOT create a new listing
        verify(listingRepository, never())
                .save(any(JobSourceListing.class));
    }

    @Test
    void shouldFindListingBySourceAndExternalJobId() {

        // Arrange
        JobSource source = mock(JobSource.class);

        when(source.getId()).thenReturn(1L);
        when(source.getCode()).thenReturn("GREENHOUSE");

        JobSourceListing listing =
                mock(JobSourceListing.class);

        when(jobSourceService.getByCode("greenhouse"))
                .thenReturn(source);

        when(listingRepository.findByJobSourceIdAndExternalJobId(
                1L,
                "12345"
        )).thenReturn(Optional.of(listing));

        // Act
        JobSourceListing result =
                listingService.getBySourceAndExternalJobId(
                        "greenhouse",
                        "12345"
                );

        // Assert
        assertThat(result).isSameAs(listing);

        verify(jobSourceService)
                .getByCode("greenhouse");

        verify(listingRepository)
                .findByJobSourceIdAndExternalJobId(
                        1L,
                        "12345"
                );
    }

    @Test
    void shouldThrowWhenListingDoesNotExist() {

        // Arrange
        JobSource source = mock(JobSource.class);

        when(source.getId()).thenReturn(1L);
        when(source.getCode()).thenReturn("GREENHOUSE");

        when(jobSourceService.getByCode("greenhouse"))
                .thenReturn(source);

        when(listingRepository.findByJobSourceIdAndExternalJobId(
                1L,
                "12345"
        )).thenReturn(Optional.empty());

        // Act + Assert
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        listingService.getBySourceAndExternalJobId(
                                "greenhouse",
                                "12345"
                        )
                )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Job source listing not found for source " +
                                "GREENHOUSE" +
                                " and external job ID " +
                                "12345"
                );

        verify(jobSourceService)
                .getByCode("greenhouse");

        verify(listingRepository)
                .findByJobSourceIdAndExternalJobId(
                        1L,
                        "12345"
                );
    }
}