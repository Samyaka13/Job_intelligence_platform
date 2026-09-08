package com.samyak.job_intelligence.job.service.requirment;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.RuleBasedRequirementExtractor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedRequirementExtractorTest {

    private final RuleBasedRequirementExtractor extractor =
            new RuleBasedRequirementExtractor();

    @Test
    void shouldExtractMandatoryTechnologies() {

        List<ExtractedJobRequirement> result =
                extractor.extract(
                        "Must have Java and Spring Boot."
                );

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(ExtractedJobRequirement::value)
                .containsExactly(
                        "Java",
                        "Spring Boot"
                );

        assertThat(result)
                .allMatch(
                        ExtractedJobRequirement::mandatory
                );

        assertThat(result)
                .allMatch(
                        requirement ->
                                requirement.requirementType()
                                        == RequirementType.TECHNOLOGY
                );
    }

    @Test
    void shouldExtractNonMandatoryTechnology() {

        List<ExtractedJobRequirement> result =
                extractor.extract(
                        "Kafka is a plus."
                );

        assertThat(result)
                .hasSize(1);

        ExtractedJobRequirement requirement =
                result.getFirst();

        assertThat(requirement.value())
                .isEqualTo("Kafka");

        assertThat(requirement.mandatory())
                .isFalse();

        assertThat(requirement.requirementType())
                .isEqualTo(RequirementType.TECHNOLOGY);
    }

    @Test
    void shouldReturnEmptyForBlankDescription() {

        assertThat(
                extractor.extract(" ")
        ).isEmpty();
    }

    @Test
    void shouldPreserveRequirementText() {

        String sentence =
                "Java is required.";

        List<ExtractedJobRequirement> result =
                extractor.extract(sentence);

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().requirementText())
                .isEqualTo("Java is required");
    }
}