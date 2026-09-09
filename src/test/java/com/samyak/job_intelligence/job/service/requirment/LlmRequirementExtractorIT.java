package com.samyak.job_intelligence.job.service.requirment;



import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.llm.LlmRequirementExtractor;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LlmRequirementExtractorIT {

    @Autowired
    private LlmRequirementExtractor llmRequirementExtractor;

    @Test
    void shouldExtractAndMapRequirementsUsingGemini() {

        Assumptions.assumeTrue(
                System.getenv("GEMINI_API_KEY") != null &&
                        !System.getenv("GEMINI_API_KEY").isBlank(),
                "GEMINI_API_KEY is not configured"
        );

        String jobDescription = """
                We are looking for a backend engineer with 2-4 years of experience.

                Strong Java and Spring Boot experience is required.
                Experience with Kafka is a plus.
                Familiarity with AWS would be beneficial.

                Experience with Docker is preferred.
                """;

        List<ExtractedJobRequirement> requirements =
                llmRequirementExtractor.extract(jobDescription);

        assertThat(requirements)
                .isNotEmpty();

        ExtractedJobRequirement javaRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.normalizedValue()
                                        .equals("java"))
                        .findFirst()
                        .orElseThrow();

        assertThat(javaRequirement.requirementType())
                .isEqualTo(RequirementType.TECHNOLOGY);

        assertThat(javaRequirement.mandatory())
                .isTrue();

        ExtractedJobRequirement springBootRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.normalizedValue()
                                        .equals("spring boot"))
                        .findFirst()
                        .orElseThrow();

        assertThat(springBootRequirement.mandatory())
                .isTrue();

        ExtractedJobRequirement kafkaRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.normalizedValue()
                                        .equals("kafka"))
                        .findFirst()
                        .orElseThrow();

        assertThat(kafkaRequirement.mandatory())
                .isFalse();

        ExtractedJobRequirement awsRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.normalizedValue()
                                        .equals("aws"))
                        .findFirst()
                        .orElseThrow();

        assertThat(awsRequirement.mandatory())
                .isFalse();

        ExtractedJobRequirement dockerRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.normalizedValue()
                                        .equals("docker"))
                        .findFirst()
                        .orElseThrow();

        assertThat(dockerRequirement.mandatory())
                .isFalse();

        assertThat(requirements)
                .noneMatch(requirement ->
                        requirement.requirementType()
                                == RequirementType.EXPERIENCE
                );
    }
}