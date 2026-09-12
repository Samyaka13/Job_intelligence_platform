package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.domain.JobStatus;
import com.samyak.job_intelligence.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JobBatchMatchEvaluationServiceTest {

    private JobRepository jobRepository;
    private JobMatchEvaluationService jobMatchEvaluationService;
    private JobBatchMatchEvaluationService service;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        jobMatchEvaluationService = mock(JobMatchEvaluationService.class);

        service = new JobBatchMatchEvaluationService(
                jobRepository,
                jobMatchEvaluationService
        );
    }

    @Test
    void shouldEvaluateAllActiveJobsForCandidate() {

        Long candidateProfileId = 10L;

        Job job1 = mock(Job.class);
        Job job2 = mock(Job.class);
        Job job3 = mock(Job.class);

        JobMatch match1 = mock(JobMatch.class);
        JobMatch match2 = mock(JobMatch.class);
        JobMatch match3 = mock(JobMatch.class);

        when(job1.getId()).thenReturn(1L);
        when(job2.getId()).thenReturn(2L);
        when(job3.getId()).thenReturn(3L);

        when(jobRepository.findByStatusOrderByPostedAtDesc(JobStatus.ACTIVE))
                .thenReturn(List.of(job1, job2, job3));

        when(jobMatchEvaluationService.evaluate(1L, candidateProfileId))
                .thenReturn(match1);

        when(jobMatchEvaluationService.evaluate(2L, candidateProfileId))
                .thenReturn(match2);

        when(jobMatchEvaluationService.evaluate(3L, candidateProfileId))
                .thenReturn(match3);

        List<JobMatch> result = service.evaluate(candidateProfileId);

        assertThat(result)
                .containsExactly(match1, match2, match3);

        verify(jobRepository)
                .findByStatusOrderByPostedAtDesc(JobStatus.ACTIVE);

        verify(jobMatchEvaluationService)
                .evaluate(1L, candidateProfileId);

        verify(jobMatchEvaluationService)
                .evaluate(2L, candidateProfileId);

        verify(jobMatchEvaluationService)
                .evaluate(3L, candidateProfileId);

        verifyNoMoreInteractions(jobRepository, jobMatchEvaluationService);
    }
}