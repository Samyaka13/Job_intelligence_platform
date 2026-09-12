package com.samyak.job_intelligence.job.service.digest;

import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.service.matching.CandidateJobMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DailyDigestServiceTest {

    private CandidateJobMatchingService candidateJobMatchingService;
    private DailyDigestService service;

    @BeforeEach
    void setUp() {
        candidateJobMatchingService = mock(CandidateJobMatchingService.class);

        service = new DailyDigestService(
                candidateJobMatchingService
        );
    }

    @Test
    void shouldGenerateDigestWithRankedMatchesRespectingLimit() {

        Long candidateProfileId = 10L;
        int limit = 3;

        JobMatch match1 = mock(JobMatch.class);
        JobMatch match2 = mock(JobMatch.class);
        JobMatch match3 = mock(JobMatch.class);
        JobMatch match4 = mock(JobMatch.class);
        JobMatch match5 = mock(JobMatch.class);

        List<JobMatch> rankedMatches = List.of(
                match1,
                match2,
                match3,
                match4,
                match5
        );

        when(candidateJobMatchingService.evaluateAndRank(candidateProfileId))
                .thenReturn(rankedMatches);

        Instant before = Instant.now();

        DailyDigest digest =
                service.generate(candidateProfileId, limit);

        Instant after = Instant.now();

        verify(candidateJobMatchingService)
                .evaluateAndRank(candidateProfileId);

        assertThat(digest.candidateProfileId())
                .isEqualTo(candidateProfileId);

        assertThat(digest.matches())
                .containsExactly(
                        match1,
                        match2,
                        match3
                );

        assertThat(digest.matches())
                .hasSize(limit);

        assertThat(digest.generatedAt())
                .isNotNull();

        assertThat(digest.generatedAt())
                .isBetween(before, after);
    }
}