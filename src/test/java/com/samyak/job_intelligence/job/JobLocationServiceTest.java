package com.samyak.job_intelligence.job;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.domain.RemoteType;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobLocationServiceTest {

    @Mock
    private JobLocationRepository jobLocationRepository;

    @Mock
    private JobService jobService;

    @Test
    void shouldReplaceExistingLocations() {

        Long jobId = 1L;

        Job job = mock(Job.class);

        JobLocation existingLocation =
                new JobLocation(
                        job,
                        "Delhi",
                        "Delhi",
                        "India",
                        RemoteType.UNKNOWN,
                        "Delhi, India"
                );

        NormalizedJobLocation bangalore =
                new NormalizedJobLocation(
                        "Bangalore",
                        "Karnataka",
                        "India",
                        "Bangalore, Karnataka, India"
                );

        NormalizedJobLocation pune =
                new NormalizedJobLocation(
                        "Pune",
                        "Maharashtra",
                        "India",
                        "Pune, Maharashtra, India"
                );

        when(jobService.getById(jobId))
                .thenReturn(job);

        when(jobLocationRepository.findByJobId(jobId))
                .thenReturn(List.of(existingLocation));

        JobLocationService service =
                new JobLocationService(
                        jobLocationRepository,
                        jobService
                );

        service.replaceLocations(
                jobId,
                List.of(bangalore, pune)
        );

        verify(jobLocationRepository)
                .deleteAll(List.of(existingLocation));

        ArgumentCaptor<List<JobLocation>> captor =
                ArgumentCaptor.captor();

        verify(jobLocationRepository)
                .saveAll(captor.capture());

        List<JobLocation> savedLocations = captor.getValue();

        assertThat(savedLocations)
                .hasSize(2);

        assertThat(savedLocations)
                .extracting(JobLocation::getCity)
                .containsExactly("Bangalore", "Pune");

        assertThat(savedLocations)
                .extracting(JobLocation::getState)
                .containsExactly("Karnataka", "Maharashtra");

        assertThat(savedLocations)
                .extracting(JobLocation::getCountry)
                .containsOnly("India");

        assertThat(savedLocations)
                .extracting(JobLocation::getRemoteType)
                .containsOnly(RemoteType.UNKNOWN);

        assertThat(savedLocations)
                .extracting(JobLocation::getJob)
                .containsOnly(job);
    }

    @Test
    void shouldNotSaveLocationsWhenInputIsEmpty() {

        Long jobId = 1L;

        Job job = mock(Job.class);

        when(jobService.getById(jobId))
                .thenReturn(job);

        when(jobLocationRepository.findByJobId(jobId))
                .thenReturn(List.of());

        JobLocationService service =
                new JobLocationService(
                        jobLocationRepository,
                        jobService
                );

        service.replaceLocations(
                jobId,
                List.of()
        );

        verify(jobLocationRepository)
                .deleteAll(List.of());

        verify(jobLocationRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldNotSaveLocationsWhenInputIsNull() {

        Long jobId = 1L;

        Job job = mock(Job.class);

        when(jobService.getById(jobId))
                .thenReturn(job);

        when(jobLocationRepository.findByJobId(jobId))
                .thenReturn(List.of());

        JobLocationService service =
                new JobLocationService(
                        jobLocationRepository,
                        jobService
                );

        service.replaceLocations(jobId, null);

        verify(jobLocationRepository)
                .deleteAll(List.of());

        verify(jobLocationRepository, never())
                .saveAll(anyList());
    }
}
