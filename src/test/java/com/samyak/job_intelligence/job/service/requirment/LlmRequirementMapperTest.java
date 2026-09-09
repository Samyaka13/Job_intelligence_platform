package com.samyak.job_intelligence.job.service.requirment;



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
                                "  Spring Boot  ",
                                true,
                                null,
                                "Spring Boot is required."
                        )
                );

        List<ExtractedJobRequirement> result =
                mapper.map(input);

        assertThat(result).hasSize(1);

        ExtractedJobRequirement requirement =
                result.getFirst();

        assertThat(requirement.value())
                .isEqualTo("Spring Boot");

        assertThat(requirement.normalizedValue())
                .isEqualTo("spring boot");

        assertThat(requirement.mandatory())
                .isTrue();
    }

    @Test
    void shouldRejectBlankValue() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.TECHNOLOGY,
                                " ",
                                true,
                                null,
                                "Invalid requirement."
                        )
                );

        assertThatThrownBy(() -> mapper.map(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Requirement value cannot be blank");
    }

    @Test
    void shouldRejectNegativeYears() {

        List<LlmExtractedRequirement> input =
                List.of(
                        new LlmExtractedRequirement(
                                RequirementType.EXPERIENCE,
                                "Java",
                                true,
                                BigDecimal.valueOf(-2),
                                "Invalid experience."
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