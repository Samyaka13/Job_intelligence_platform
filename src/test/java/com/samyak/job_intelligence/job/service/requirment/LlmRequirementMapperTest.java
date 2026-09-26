package com.samyak.job_intelligence.job.service.requirment;

import com.samyak.job_intelligence.job.domain.RequirementMatchMode;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.LlmExtractedRequirement;
import com.samyak.job_intelligence.llm.LlmRequirementMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmRequirementMapperTest {

    private final LlmRequirementMapper mapper =
            new LlmRequirementMapper();

    @Test
    void shouldNormalizeLlmRequirement() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.TECHNOLOGY,
                                List.of("  Spring Boot  "),
                                true,
                                null,
                                "Spring Boot is required.",
                                RequirementMatchMode.SINGLE
                        )
                );

        List<ExtractedJobRequirement> result =
                mapper.map(input);

        assertThat(result)
                .hasSize(1);

        ExtractedJobRequirement requirement =
                result.getFirst();

        assertThat(requirement.value())
                .isEqualTo("Spring Boot");

        assertThat(requirement.normalizedValue())
                .isEqualTo("spring boot");

        assertThat(requirement.mandatory())
                .isTrue();

        assertThat(requirement.requirementMatchMode())
                .isEqualTo(RequirementMatchMode.SINGLE);

        assertThat(requirement.groupId())
                .isNotBlank();
    }

    @Test
    void shouldMapAnyOfRequirementIntoMultipleAtomicRequirements() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.TECHNOLOGY,
                                List.of("Java", "Python", "Go"),
                                true,
                                null,
                                "Experience with Java, Python, or Go.",
                                RequirementMatchMode.ANY_OF
                        )
                );

        List<ExtractedJobRequirement> result =
                mapper.map(input);

        assertThat(result)
                .hasSize(3);

        assertThat(result)
                .extracting(ExtractedJobRequirement::value)
                .containsExactly(
                        "Java",
                        "Python",
                        "Go"
                );

        assertThat(result)
                .allMatch(requirement ->
                        requirement.requirementMatchMode() == RequirementMatchMode.ANY_OF
                );

        assertThat(result)
                .allMatch(requirement ->
                        requirement.groupId() != null
                                && !requirement.groupId().isBlank()
                );

        assertThat(result.get(0).groupId())
                .isEqualTo(result.get(1).groupId());

        assertThat(result.get(1).groupId())
                .isEqualTo(result.get(2).groupId());
    }

    @Test
    void shouldMapAllOfRequirementIntoMultipleAtomicRequirements() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.TECHNOLOGY,
                                List.of("Java", "Spring Boot"),
                                true,
                                null,
                                "Strong Java and Spring Boot experience.",
                                RequirementMatchMode.ALL_OF
                        )
                );

        List<ExtractedJobRequirement> result =
                mapper.map(input);

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(ExtractedJobRequirement::value)
                .containsExactly(
                        "Java",
                        "Spring Boot"
                );

        assertThat(result)
                .allMatch(requirement ->
                        requirement.requirementMatchMode() == RequirementMatchMode.ALL_OF
                );

        assertThat(result.get(0).groupId())
                .isEqualTo(result.get(1).groupId());
    }

    @Test
    void shouldRejectBlankValue() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.TECHNOLOGY,
                                List.of(" "),
                                true,
                                null,
                                "Invalid requirement.",
                                RequirementMatchMode.SINGLE
                        )
                );

        assertThatThrownBy(() -> mapper.map(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Requirement values cannot contain blank values");
    }

    @Test
    void shouldRejectNegativeYears() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.EXPERIENCE,
                                List.of("Java"),
                                true,
                                BigDecimal.valueOf(-2),
                                "Invalid experience.",
                                RequirementMatchMode.SINGLE
                        )
                );

        assertThatThrownBy(() -> mapper.map(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Required years cannot be negative");
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {

        assertThat(
                mapper.map(List.of())
        ).isEmpty();

        assertThat(
                mapper.map(null)
        ).isEmpty();
    }
}