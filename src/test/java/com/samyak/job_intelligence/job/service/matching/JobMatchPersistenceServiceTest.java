package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.repository.JobMatchRepository;
import com.samyak.job_intelligence.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobMatchPersistenceServiceTest {

    @Mock
    private JobMatchRepository jobMatchRepository;

    @Mock
    private CandidateProfileRepository candidateProfileRepository;

    @Mock
    private JobService jobService;

    @Mock
    private JobMatchPersistenceMapper persistenceMapper;

    @Mock
    private JobMatchResult jobMatchResult;

    @Test
    void shouldCreateNewJobMatchWhenMatchDoesNotExist() {

        Long jobId = 1L;
        Long candidateProfileId = 2L;
        Instant evaluatedAt = Instant.now();

        Job job = mock(Job.class);
        CandidateProfile candidateProfile = mock(CandidateProfile.class);

        JobMatchPersistenceData data =
                new JobMatchPersistenceData(
                        true,
                        new BigDecimal("85.00"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("85.00"),
                        "Matched skills: java; Missing skills: docker",
                        null,
                        null,
                        null,
                        evaluatedAt
                );

        when(jobService.getById(jobId)).thenReturn(job);
        when(candidateProfileRepository.findById(candidateProfileId))
                .thenReturn(Optional.of(candidateProfile));

        when(persistenceMapper.map(jobMatchResult, evaluatedAt))
                .thenReturn(data);

        when(jobMatchRepository.findByJobIdAndCandidateProfileId(
                jobId,
                candidateProfileId
        )).thenReturn(Optional.empty());

        JobMatch savedJobMatch = mock(JobMatch.class);

        when(jobMatchRepository.save(any(JobMatch.class)))
                .thenReturn(savedJobMatch);

        JobMatch result = new JobMatchPersistenceService(
                jobMatchRepository,
                candidateProfileRepository,
                jobService,
                persistenceMapper
        ).save(
                jobId,
                candidateProfileId,
                jobMatchResult,
                evaluatedAt
        );

        assertSame(savedJobMatch, result);

        ArgumentCaptor<JobMatch> captor =
                ArgumentCaptor.forClass(JobMatch.class);

        verify(jobMatchRepository).save(captor.capture());

        JobMatch captured = captor.getValue();

        assertSame(job, captured.getJob());
        assertSame(candidateProfile, captured.getCandidateProfile());
        assertTrue(captured.isHardQualified());
        assertEquals(new BigDecimal("85.00"), captured.getSkillScore());
        assertEquals(new BigDecimal("85.00"), captured.getFinalScore());
        assertEquals(
                "Matched skills: java; Missing skills: docker",
                captured.getMatchReasoning()
        );
        assertEquals(evaluatedAt, captured.getEvaluatedAt());
    }

    @Test
    void shouldUpdateExistingJobMatchWhenMatchAlreadyExists() {

        Long jobId = 1L;
        Long candidateProfileId = 2L;
        Instant evaluatedAt = Instant.now();

        Job job = mock(Job.class);
        CandidateProfile candidateProfile = mock(CandidateProfile.class);

        JobMatch existingJobMatch =
                new JobMatch(
                        job,
                        candidateProfile,
                        true,
                        new BigDecimal("70.00"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("70.00"),
                        "Old reasoning",
                        null,
                        null,
                        null,
                        evaluatedAt.minusSeconds(100)
                );

        JobMatchPersistenceData data =
                new JobMatchPersistenceData(
                        true,
                        new BigDecimal("90.00"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("90.00"),
                        "New reasoning",
                        null,
                        null,
                        null,
                        evaluatedAt
                );

        when(jobService.getById(jobId)).thenReturn(job);

        when(candidateProfileRepository.findById(candidateProfileId))
                .thenReturn(Optional.of(candidateProfile));

        when(persistenceMapper.map(jobMatchResult, evaluatedAt))
                .thenReturn(data);

        when(jobMatchRepository.findByJobIdAndCandidateProfileId(
                jobId,
                candidateProfileId
        )).thenReturn(Optional.of(existingJobMatch));

        when(jobMatchRepository.save(existingJobMatch))
                .thenReturn(existingJobMatch);

        JobMatch result = new JobMatchPersistenceService(
                jobMatchRepository,
                candidateProfileRepository,
                jobService,
                persistenceMapper
        ).save(
                jobId,
                candidateProfileId,
                jobMatchResult,
                evaluatedAt
        );

        assertSame(existingJobMatch, result);

        assertTrue(existingJobMatch.isHardQualified());
        assertEquals(
                new BigDecimal("90.00"),
                existingJobMatch.getSkillScore()
        );
        assertEquals(
                new BigDecimal("90.00"),
                existingJobMatch.getFinalScore()
        );
        assertEquals(
                "New reasoning",
                existingJobMatch.getMatchReasoning()
        );
        assertEquals(
                evaluatedAt,
                existingJobMatch.getEvaluatedAt()
        );

        verify(jobMatchRepository).save(existingJobMatch);
    }
}