package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class DailyMatchingSchedulerTest {

    private CandidateProfileRepository candidateProfileRepository;
    private CandidateJobMatchingService candidateJobMatchingService;
    private DailyMatchingScheduler scheduler;

    @BeforeEach
    void setUp() {
        candidateProfileRepository = mock(CandidateProfileRepository.class);
        candidateJobMatchingService = mock(CandidateJobMatchingService.class);

        scheduler = new DailyMatchingScheduler(
                candidateProfileRepository,
                candidateJobMatchingService
        );
    }

    @Test
    void shouldRunMatchingForAllActiveCandidates() {

        CandidateProfile candidate1 = mock(CandidateProfile.class);
        CandidateProfile candidate2 = mock(CandidateProfile.class);

        when(candidate1.getId()).thenReturn(1L);
        when(candidate2.getId()).thenReturn(2L);

        when(candidateProfileRepository.findByIsActiveTrue())
                .thenReturn(List.of(candidate1, candidate2));

        scheduler.runDailyMatching();

        verify(candidateJobMatchingService)
                .evaluateAndRank(1L);

        verify(candidateJobMatchingService)
                .evaluateAndRank(2L);
    }

    @Test
    void shouldContinueWhenOneCandidateFails() {

        CandidateProfile candidate1 = mock(CandidateProfile.class);
        CandidateProfile candidate2 = mock(CandidateProfile.class);

        when(candidate1.getId()).thenReturn(1L);
        when(candidate2.getId()).thenReturn(2L);

        when(candidateProfileRepository.findByIsActiveTrue())
                .thenReturn(List.of(candidate1, candidate2));

        doThrow(new RuntimeException("matching failed"))
                .when(candidateJobMatchingService)
                .evaluateAndRank(1L);

        scheduler.runDailyMatching();

        verify(candidateJobMatchingService)
                .evaluateAndRank(1L);

        verify(candidateJobMatchingService)
                .evaluateAndRank(2L);
    }
}


