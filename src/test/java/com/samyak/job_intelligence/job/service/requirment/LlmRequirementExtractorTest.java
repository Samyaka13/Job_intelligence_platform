package com.samyak.job_intelligence.job.service.requirment;

import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.*;
import com.samyak.job_intelligence.llm.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("llm")
@ExtendWith(MockitoExtension.class)
class LlmRequirementExtractorTest {

    @Mock
    private LlmClient llmClient;

    @Mock
    private LlmRequirementPromptBuilder promptBuilder;

    @Mock
    private LlmRequirementMapper requirementMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldExtractRequirementsFromLlmResponse() throws Exception {

        String jobDescription =
                "Strong Java experience is required. Kafka is a plus.";

        String systemPrompt =
                "system prompt";

        String userPrompt =
                "user prompt";

        String llmResponse =
                """
                {
                  "requirements": [
                    {
                      "requirementType": "TECHNOLOGY",
                      "value": "Java",
                      "mandatory": true,
                      "yearsRequired": null,
                      "requirementText": "Strong Java experience is required."
                    },
                    {
                      "requirementType": "TECHNOLOGY",
                      "value": "Kafka",
                      "mandatory": false,
                      "yearsRequired": null,
                      "requirementText": "Kafka is a plus."
                    }
                  ]
                }
                """;

        LlmRequirementExtractionResponse extractionResponse =
                new LlmRequirementExtractionResponse(
                        List.of(
                                new LlmExtractedRequirement(
                                        RequirementType.TECHNOLOGY,
                                        "Java",
                                        true,
                                        null,
                                        "Strong Java experience is required."
                                ),
                                new LlmExtractedRequirement(
                                        RequirementType.TECHNOLOGY,
                                        "Kafka",
                                        false,
                                        null,
                                        "Kafka is a plus."
                                )
                        )
                );

        List<ExtractedJobRequirement> mappedRequirements =
                List.of(
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Java",
                                "java",
                                true,
                                null,
                                "Strong Java experience is required."
                        ),
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Kafka",
                                "kafka",
                                false,
                                null,
                                "Kafka is a plus."
                        )
                );

        when(promptBuilder.systemPrompt())
                .thenReturn(systemPrompt);

        when(promptBuilder.userPrompt(jobDescription))
                .thenReturn(userPrompt);

        when(llmClient.generate(systemPrompt, userPrompt))
                .thenReturn(llmResponse);

        when(objectMapper.readValue(
                llmResponse,
                LlmRequirementExtractionResponse.class
        )).thenReturn(extractionResponse);

        when(requirementMapper.map(
                extractionResponse.requirements()
        )).thenReturn(mappedRequirements);

        LlmRequirementExtractor extractor =
                new LlmRequirementExtractor(
                        llmClient,
                        promptBuilder,
                        requirementMapper,
                        objectMapper
                );

        List<ExtractedJobRequirement> result =
                extractor.extract(jobDescription);

        assertThat(result)
                .hasSize(2);

        assertThat(result.getFirst().value())
                .isEqualTo("Java");

        assertThat(result.getFirst().mandatory())
                .isTrue();

        assertThat(result.get(1).value())
                .isEqualTo("Kafka");

        assertThat(result.get(1).mandatory())
                .isFalse();

        verify(llmClient)
                .generate(systemPrompt, userPrompt);

        verify(requirementMapper)
                .map(extractionResponse.requirements());
    }

    @Test
    void shouldReturnEmptyForBlankDescription() {

        LlmRequirementExtractor extractor =
                new LlmRequirementExtractor(
                        llmClient,
                        promptBuilder,
                        requirementMapper,
                        objectMapper
                );

        assertThat(extractor.extract(" "))
                .isEmpty();

        verifyNoInteractions(
                llmClient,
                promptBuilder,
                requirementMapper,
                objectMapper
        );
    }

    @Test
    void shouldFailWhenLlmResponseCannotBeParsed()
            throws Exception {

        String jobDescription =
                "Java experience is required.";

        when(promptBuilder.systemPrompt())
                .thenReturn("system");

        when(promptBuilder.userPrompt(jobDescription))
                .thenReturn("user");

        when(llmClient.generate("system", "user"))
                .thenReturn("not valid json");

        when(objectMapper.readValue(
                "not valid json",
                LlmRequirementExtractionResponse.class
        )).thenThrow(
                new IllegalArgumentException("invalid json")
        );

        LlmRequirementExtractor extractor =
                new LlmRequirementExtractor(
                        llmClient,
                        promptBuilder,
                        requirementMapper,
                        objectMapper
                );

        assertThat(
                org.assertj.core.api.Assertions.catchThrowable(
                        () -> extractor.extract(jobDescription)
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Failed to parse LLM requirement extraction response"
                );
    }
}