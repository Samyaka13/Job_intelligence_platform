package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JobMatchEvaluationServiceTest {

    private JobService jobService;
    private JobLocationRepository jobLocationRepository;
    private CandidateProfileRepository candidateProfileRepository;
    private JobMatchingService jobMatchingService;
    private JobMatchPersistenceService jobMatchPersistenceService;

    private JobMatchEvaluationService service;

    @BeforeEach
    void setUp() {

        jobService = mock(JobService.class);
        jobLocationRepository = mock(JobLocationRepository.class);
        candidateProfileRepository = mock(CandidateProfileRepository.class);
        jobMatchingService = mock(JobMatchingService.class);
        jobMatchPersistenceService = mock(JobMatchPersistenceService.class);

        service = new JobMatchEvaluationService(
                jobService,
                jobLocationRepository,
                candidateProfileRepository,
                jobMatchingService,
                jobMatchPersistenceService
        );
    }

    @Test
    void shouldEvaluateAndPersistJobMatch() {

        // Given
        Long jobId = 20L;
        Long candidateProfileId = 10L;

        Job job = mock(Job.class);
        CandidateProfile candidateProfile = mock(CandidateProfile.class);

        JobLocation location1 = mock(JobLocation.class);
        JobLocation location2 = mock(JobLocation.class);

        List<JobLocation> locations = List.of(
                location1,
                location2
        );

        JobMatchResult jobMatchResult = mock(JobMatchResult.class);
        JobMatch expectedJobMatch = mock(JobMatch.class);

        when(jobService.getById(jobId))
                .thenReturn(job);

        when(candidateProfileRepository.findById(candidateProfileId))
                .thenReturn(Optional.of(candidateProfile));

        when(jobLocationRepository.findByJobId(jobId))
                .thenReturn(locations);

        when(jobMatchingService.match(any(JobMatchingInput.class)))
                .thenReturn(jobMatchResult);

        when(jobMatchPersistenceService.save(
                eq(jobId),
                eq(candidateProfileId),
                eq(jobMatchResult),
                any()
        )).thenReturn(expectedJobMatch);

        // When
        JobMatch actualJobMatch =
                service.evaluate(
                        jobId,
                        candidateProfileId
                );

        // Then
        assertEquals(
                expectedJobMatch,
                actualJobMatch
        );

        verify(jobService)
                .getById(jobId);

        verify(candidateProfileRepository)
                .findById(candidateProfileId);

        verify(jobLocationRepository)
                .findByJobId(jobId);

        ArgumentCaptor<JobMatchingInput> inputCaptor =
                ArgumentCaptor.forClass(JobMatchingInput.class);

        verify(jobMatchingService)
                .match(inputCaptor.capture());

        JobMatchingInput capturedInput =
                inputCaptor.getValue();

        assertEquals(
                job,
                capturedInput.job()
        );

        assertEquals(
                candidateProfile,
                capturedInput.candidateProfile()
        );

        assertEquals(
                locations,
                capturedInput.locations()
        );

        verify(jobMatchPersistenceService)
                .save(
                        eq(jobId),
                        eq(candidateProfileId),
                        eq(jobMatchResult),
                        any()
                );
    }
}