package com.samyak.job_intelligence.job.service.requirment;



import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.LlmExtractedRequirement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LlmExtractedRequirementTest {

    @Test
    void shouldCreateLlmExtractedRequirement() {

        LlmExtractedRequirement requirement =
                new LlmExtractedRequirement(
                        RequirementType.TECHNOLOGY,
                        "Java",
                        true,
                        BigDecimal.valueOf(3),
                        "3 years of Java experience required."
                );

        assertThat(requirement.requirementType())
                .isEqualTo(RequirementType.TECHNOLOGY);

        assertThat(requirement.value())
                .isEqualTo("Java");

        assertThat(requirement.mandatory())
                .isTrue();

        assertThat(requirement.yearsRequired())
                .isEqualByComparingTo("3");

        assertThat(requirement.requirementText())
                .isEqualTo("3 years of Java experience required.");
    }
}