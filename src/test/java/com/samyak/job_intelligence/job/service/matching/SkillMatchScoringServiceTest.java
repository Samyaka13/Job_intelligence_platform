package com.samyak.job_intelligence.job.service.matching;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SkillMatchScoringServiceTest {

    private final SkillMatchScoringService service =
            new SkillMatchScoringService();

    @Test
    void shouldCalculatePerfectSkillMatch() {

        SkillMatchResult result = new SkillMatchResult(
                List.of("java", "spring boot", "kafka"),
                List.of(),
                List.of(),
                3,
                2
        );

        SkillMatchScore score = service.calculate(result);

        assertEquals(
                new BigDecimal("1.0000"),
                score.score()
        );

        assertEquals(
                new BigDecimal("1.0000"),
                score.matchedRequirementRatio()
        );

        assertEquals(
                new BigDecimal("1.0000"),
                score.mandatoryMatchRatio()
        );

        assertFalse(score.hasMissingMandatorySkills());
    }

    @Test
    void shouldCalculatePartialSkillMatch() {

        SkillMatchResult result = new SkillMatchResult(
                List.of("java", "spring boot"),//matchedSkill
                List.of("kafka", "aws"),//missingSkill
                List.of(),
                4,
                2
        );

        SkillMatchScore score = service.calculate(result);

        assertEquals(
                new BigDecimal("0.8000"),
                score.score()
        );

        assertEquals(
                new BigDecimal("0.5000"),
                score.matchedRequirementRatio()
        );

        assertEquals(
                new BigDecimal("1.0000"),
                score.mandatoryMatchRatio()
        );

        assertFalse(score.hasMissingMandatorySkills());
    }

    @Test
    void shouldCalculatePartialMandatoryMatch() {

        SkillMatchResult result = new SkillMatchResult(
                List.of("java"),//matchedSkill
                List.of("spring boot", "kafka"),//missingSkill
                List.of("spring boot"),//missingMandatorySkills
                3,//totalRequirements
                2//mandatoryRequirements
        );

        SkillMatchScore score = service.calculate(result);

        assertEquals(
                new BigDecimal("0.4333"),
                score.score()
        );

        assertEquals(
                new BigDecimal("0.3333"),
                score.matchedRequirementRatio()
        );

        assertEquals(
                new BigDecimal("0.5000"),
                score.mandatoryMatchRatio()
        );

        assertTrue(score.hasMissingMandatorySkills());
    }

    @Test
    void shouldReturnZeroMandatoryRatioWhenThereAreNoMandatoryRequirements() {

        SkillMatchResult result = new SkillMatchResult(
                List.of("java"),
                List.of("kafka"),
                List.of(),
                2,
                0
        );

        SkillMatchScore score = service.calculate(result);

        assertEquals(
                new BigDecimal("0.2000"),
                score.score()
        );

        assertEquals(
                new BigDecimal("0.5000"),
                score.matchedRequirementRatio()
        );

        assertEquals(
                BigDecimal.ZERO,
                score.mandatoryMatchRatio()
        );

        assertFalse(score.hasMissingMandatorySkills());
    }

    @Test
    void shouldReturnZeroWhenThereAreNoRequirements() {

        SkillMatchResult result = new SkillMatchResult(
                List.of(),
                List.of(),
                List.of(),
                0,
                0
        );

        SkillMatchScore score = service.calculate(result);

        assertEquals(BigDecimal.ZERO, score.score());
        assertEquals(BigDecimal.ZERO, score.matchedRequirementRatio());
        assertEquals(BigDecimal.ZERO, score.mandatoryMatchRatio());
        assertFalse(score.hasMissingMandatorySkills());
    }

    @Test
    void shouldMarkScoreWhenMandatorySkillIsMissing() {

        SkillMatchResult result = new SkillMatchResult(
                List.of("java", "kafka"),
                List.of("spring boot"),
                List.of("spring boot"),
                3,
                2
        );

        SkillMatchScore score = service.calculate(result);

        assertTrue(score.hasMissingMandatorySkills());

        assertEquals(
                new BigDecimal("0.6667"),
                score.matchedRequirementRatio()
        );

        assertEquals(
                new BigDecimal("0.5000"),
                score.mandatoryMatchRatio()
        );
    }

    @Test
    void shouldCalculateWeightedSkillScore() {
        SkillMatchResult matchResult = new SkillMatchResult(
                List.of("java", "spring boot"),
                List.of("kafka"),
                List.of(),
                3,
                2
        );

        SkillMatchScore result = service.calculate(matchResult);

        // Overall match = 2 / 3 = 0.6667
        // Mandatory match = 2 / 2 = 1.0000
        // Score = (0.6667 * 0.4) + (1.0000 * 0.6) = 0.86668 -> 0.8667

        assertThat(result.score())
                .isEqualByComparingTo("0.8667");

        assertThat(result.matchedRequirementRatio())
                .isEqualByComparingTo("0.6667");

        assertThat(result.mandatoryMatchRatio())
                .isEqualByComparingTo("1.0000");

        assertThat(result.hasMissingMandatorySkills())
                .isFalse();
    }
}