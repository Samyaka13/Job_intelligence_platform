package com.samyak.job_intelligence.job.service.requirment;
import com.samyak.job_intelligence.job.service.requirement.LlmRequirementPromptBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmRequirementPromptBuilderTest {

    private final LlmRequirementPromptBuilder builder =
            new LlmRequirementPromptBuilder();

    @Test
    void shouldBuildSystemPrompt() {

        String prompt = builder.systemPrompt();

        assertThat(prompt)
                .contains("TECHNOLOGY")
                .contains("mandatory=true")
                .contains("mandatory=false")
                .contains("Do not invent requirements")
                .contains("yearsRequired");
    }

    @Test
    void shouldIncludeJobDescriptionInUserPrompt() {

        String description =
                "Strong Java experience is required. Kafka is a plus.";

        String prompt =
                builder.userPrompt(description);

        assertThat(prompt)
                .contains(description)
                .contains("\"requirements\"")
                .contains("\"mandatory\"")
                .contains("\"yearsRequired\"");
    }
}