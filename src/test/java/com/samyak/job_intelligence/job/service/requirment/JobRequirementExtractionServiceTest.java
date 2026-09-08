package com.samyak.job_intelligence.job.service.requirment;


import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobRequirementExtractionServiceTest {

    @Mock
    private RuleBasedRequirementExtractor ruleExtractor;

    @Mock
    private LlmRequirementExtractor llmExtractor;


    @MockitoBean
    private LlmClient llmClient;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldPreferLlmResultWhenSameRequirementExists() {

        String description =
                "Java is required. Kafka is a plus.";

        ExtractedJobRequirement ruleKafka =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Kafka",
                        "kafka",
                        true,
                        null,
                        "Kafka is a plus."
                );

        ExtractedJobRequirement llmKafka =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Kafka",
                        "kafka",
                        false,
                        null,
                        "Kafka is a plus."
                );

        when(ruleExtractor.extract(description))
                .thenReturn(List.of(ruleKafka));

        when(llmExtractor.extract(description))
                .thenReturn(List.of(llmKafka));

        JobRequirementExtractionService service =
                new JobRequirementExtractionService(
                        ruleExtractor,
                        llmExtractor
                );

        List<ExtractedJobRequirement> result =
                service.extract(description);

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().mandatory())
                .isFalse();

        verify(ruleExtractor)
                .extract(description);

        verify(llmExtractor)
                .extract(description);
    }

    @Test
    void shouldRetainRuleResultWhenLlmDoesNotReturnIt() {

        String description =
                "Must have Java and Spring Boot.";

        ExtractedJobRequirement javaRequirement =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Must have Java."
                );

        ExtractedJobRequirement springRequirement =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Spring Boot",
                        "spring boot",
                        true,
                        null,
                        "Must have Spring Boot."
                );

        when(ruleExtractor.extract(description))
                .thenReturn(List.of(javaRequirement));

        when(llmExtractor.extract(description))
                .thenReturn(List.of(springRequirement));

        JobRequirementExtractionService service =
                new JobRequirementExtractionService(
                        ruleExtractor,
                        llmExtractor
                );

        List<ExtractedJobRequirement> result =
                service.extract(description);

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(
                        ExtractedJobRequirement::normalizedValue
                )
                .containsExactly(
                        "java",
                        "spring boot"
                );
    }

    @Test
    void shouldDeduplicateSameRequirement() {

        ExtractedJobRequirement ruleResult =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Java required."
                );

        ExtractedJobRequirement llmResult =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        false,
                        null,
                        "Java preferred."
                );

        when(ruleExtractor.extract(anyString()))
                .thenReturn(List.of(ruleResult));

        when(llmExtractor.extract(anyString()))
                .thenReturn(List.of(llmResult));

        JobRequirementExtractionService service =
                new JobRequirementExtractionService(
                        ruleExtractor,
                        llmExtractor
                );

        List<ExtractedJobRequirement> result =
                service.extract("Java is preferred.");

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().mandatory())
                .isFalse();
    }

    @Test
    void shouldReturnEmptyWhenBothExtractorsReturnNothing() {

        when(ruleExtractor.extract(anyString()))
                .thenReturn(List.of());

        when(llmExtractor.extract(anyString()))
                .thenReturn(List.of());

        JobRequirementExtractionService service =
                new JobRequirementExtractionService(
                        ruleExtractor,
                        llmExtractor
                );

        assertThat(
                service.extract("Some description")
        ).isEmpty();
    }
}