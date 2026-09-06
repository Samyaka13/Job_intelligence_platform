package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.qualification.JobQualificationResult;
import com.samyak.job_intelligence.job.service.qualification.JobQualificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobMatchingServiceTest {

    private JobQualificationService jobQualificationService;
    private SkillMatchingService skillMatchingService;
    private SkillMatchScoringService skillMatchScoringService;

    private JobMatchingService service;

    @BeforeEach
    void setUp() {
        jobQualificationService = mock(JobQualificationService.class);
        skillMatchingService = mock(SkillMatchingService.class);
        skillMatchScoringService = mock(SkillMatchScoringService.class);

        service = new JobMatchingService(
                jobQualificationService,
                skillMatchingService,
                skillMatchScoringService
        );
    }

    @Test
    void shouldCombineQualificationAndSkillMatchResults() {

        NormalizedJobData job = mock(NormalizedJobData.class);
        CandidateProfile candidateProfile = mock(CandidateProfile.class);

        when(candidateProfile.getId()).thenReturn(10L);

        JobMatchingInput input = new JobMatchingInput(
                job,
                candidateProfile,
                20L
        );

        JobQualificationResult qualificationResult =
                JobQualificationResult.qualifiedListing();

        SkillMatchResult skillMatchResult = new SkillMatchResult(
                List.of("java", "spring boot"),
                List.of("kafka"),
                List.of(),
                3,
                2
        );

        SkillMatchScore skillMatchScore = new SkillMatchScore(
                new BigDecimal("0.8667"),
                new BigDecimal("0.6667"),
                new BigDecimal("1.0000"),
                false
        );

        when(jobQualificationService.qualify(job, candidateProfile))
                .thenReturn(qualificationResult);

        when(skillMatchingService.match(10L, 20L))
                .thenReturn(skillMatchResult);

        when(skillMatchScoringService.calculate(skillMatchResult))
                .thenReturn(skillMatchScore);

        JobMatchResult result = service.match(input);

        assertTrue(result.qualified());
        assertTrue(result.qualificationRejectionReasons().isEmpty());

        assertEquals(skillMatchScore, result.skillMatchScore());
        assertEquals(skillMatchResult, result.skillMatchResult());

        verify(jobQualificationService)
                .qualify(job, candidateProfile);

        verify(skillMatchingService)
                .match(10L, 20L);

        verify(skillMatchScoringService)
                .calculate(skillMatchResult);
    }
    @Test
    void shouldNotCalculateSkillMatchWhenJobFailsQualification() {

        NormalizedJobData job = mock(NormalizedJobData.class);
        CandidateProfile candidateProfile = mock(CandidateProfile.class);

        when(candidateProfile.getId()).thenReturn(10L);

        JobMatchingInput input = new JobMatchingInput(
                job,
                candidateProfile,
                20L
        );

        JobQualificationResult qualificationResult =
                JobQualificationResult.rejectedListing(
                        List.of("Required experience exceeds candidate experience")
                );

        when(jobQualificationService.qualify(job, candidateProfile))
                .thenReturn(qualificationResult);

        JobMatchResult result = service.match(input);
        assertEquals(
                List.of("Required experience exceeds candidate experience"),
                result.qualificationRejectionReasons()
        );

        assertFalse(result.qualified());
        assertNull(result.skillMatchResult());
        assertNull(result.skillMatchScore());





        verify(jobQualificationService)
                .qualify(job, candidateProfile);

        verifyNoInteractions(skillMatchingService);
        verifyNoInteractions(skillMatchScoringService);
    }

    @Test
    void shouldReturnZeroSkillScoreWhenQualifiedJobHasNoSkillRequirements() {

        NormalizedJobData job = mock(NormalizedJobData.class);
        CandidateProfile candidateProfile = mock(CandidateProfile.class);

        when(candidateProfile.getId()).thenReturn(10L);

        JobMatchingInput input = new JobMatchingInput(
                job,
                candidateProfile,
                20L
        );

        JobQualificationResult qualificationResult =
                JobQualificationResult.qualifiedListing();

        SkillMatchResult skillMatchResult = new SkillMatchResult(
                List.of(),
                List.of(),
                List.of(),
                0,
                0
        );

        SkillMatchScore skillMatchScore = new SkillMatchScore(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false
        );

        when(jobQualificationService.qualify(job, candidateProfile))
                .thenReturn(qualificationResult);

        when(skillMatchingService.match(10L, 20L))
                .thenReturn(skillMatchResult);

        when(skillMatchScoringService.calculate(skillMatchResult))
                .thenReturn(skillMatchScore);

        JobMatchResult result = service.match(input);

        assertTrue(result.qualified());

        assertEquals(
                BigDecimal.ZERO,
                result.skillMatchScore().score()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.skillMatchScore().matchedRequirementRatio()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.skillMatchScore().mandatoryMatchRatio()
        );

        assertFalse(
                result.skillMatchScore().hasMissingMandatorySkills()
        );

        verify(jobQualificationService)
                .qualify(job, candidateProfile);

        verify(skillMatchingService)
                .match(10L, 20L);

        verify(skillMatchScoringService)
                .calculate(skillMatchResult);
    }
}