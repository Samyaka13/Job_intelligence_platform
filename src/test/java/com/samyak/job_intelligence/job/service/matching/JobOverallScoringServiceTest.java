package com.samyak.job_intelligence.job.service.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class JobOverallScoringServiceTest {

    private JobOverallScoringService service;

    @BeforeEach
    void setUp() {
        service = new JobOverallScoringService();
    }

    @Test
    void shouldConvertSkillScoreToPercentage() {

        SkillMatchScore skillMatchScore = new SkillMatchScore(
                new BigDecimal("0.8667"),
                new BigDecimal("0.6667"),
                new BigDecimal("1.0000"),
                false
        );

        JobOverallScore result =
                service.calculate(skillMatchScore);

        assertEquals(
                new BigDecimal("86.67"),
                result.finalScore()
        );
    }

    @Test
    void shouldReturnNullWhenSkillScoreIsNull() {

        JobOverallScore result =
                service.calculate(null);

        assertNull(result.finalScore());
    }

    @Test
    void shouldConvertPerfectScoreTo100() {

        SkillMatchScore skillMatchScore = new SkillMatchScore(
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                false
        );

        JobOverallScore result =
                service.calculate(skillMatchScore);

        assertEquals(
                new BigDecimal("100.00"),
                result.finalScore()
        );
    }

    @Test
    void shouldPenalizeMissingMandatoryTechnicalSkillsInCompositeScore() {
        SkillMatchScore skill = new SkillMatchScore(
                new BigDecimal("0.5000"),
                new BigDecimal("0.5000"),
                new BigDecimal("0.0000"),
                true
        );

        JobOverallScore result = service.calculate(
                skill,
                new RoleMatchResult(
                        JobRoleFamily.DATA_SCIENCE,
                        new BigDecimal("50"),
                        new BigDecimal("60")
                )
        );

        assertEquals(new BigDecimal("23.18"), result.finalScore());
        assertEquals(new BigDecimal("50"), result.roleScore());
        assertEquals(new BigDecimal("60"), result.seniorityScore());
    }
}
