package com.samyak.job_intelligence.job.service.requirment;

import com.samyak.job_intelligence.job.domain.RequirementMatchMode;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.LlmExtractedRequirement;
import com.samyak.job_intelligence.llm.JobDescriptionCleaner;
import com.samyak.job_intelligence.llm.LlmClient;
import com.samyak.job_intelligence.llm.LlmRequirementExtractionResponse;
import com.samyak.job_intelligence.llm.LlmRequirementMapper;
import com.samyak.job_intelligence.llm.LlmRequirementPromptBuilder;
import com.samyak.job_intelligence.llm.LlmRequirementExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

        String cleanedUpDescription =
                JobDescriptionCleaner.clean(jobDescription);

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
                      "values": ["Java"],
                      "mandatory": true,
                      "yearsRequired": null,
                      "requirementText": "Strong Java experience is required.",
                      "matchMode": "SINGLE"
                    },
                    {
                      "requirementType": "TECHNOLOGY",
                      "values": ["Kafka"],
                      "mandatory": false,
                      "yearsRequired": null,
                      "requirementText": "Kafka is a plus.",
                      "matchMode": "SINGLE"
                    }
                  ]
                }
                """;

        LlmRequirementExtractionResponse extractionResponse =
                new LlmRequirementExtractionResponse(
                        List.of(
                                new LlmExtractedRequirement(
                                        RequirementType.TECHNOLOGY,
                                        List.of("Java"),
                                        true,
                                        null,
                                        "Strong Java experience is required.",
                                        RequirementMatchMode.SINGLE
                                ),
                                new LlmExtractedRequirement(
                                        RequirementType.TECHNOLOGY,
                                        List.of("Kafka"),
                                        false,
                                        null,
                                        "Kafka is a plus.",
                                        RequirementMatchMode.SINGLE
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
                                "Strong Java experience is required.",
                                "group-java",
                                RequirementMatchMode.SINGLE
                        ),
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Kafka",
                                "kafka",
                                false,
                                null,
                                "Kafka is a plus.",
                                "group-kafka",
                                RequirementMatchMode.SINGLE
                        )
                );

        when(promptBuilder.systemPrompt())
                .thenReturn(systemPrompt);

        when(promptBuilder.userPrompt(cleanedUpDescription))
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

        assertThat(result.getFirst().normalizedValue())
                .isEqualTo("java");

        assertThat(result.getFirst().mandatory())
                .isTrue();

        assertThat(result.getFirst().requirementMatchMode())
                .isEqualTo(RequirementMatchMode.SINGLE);

        assertThat(result.get(1).value())
                .isEqualTo("Kafka");

        assertThat(result.get(1).mandatory())
                .isFalse();

        assertThat(result.get(1).requirementMatchMode())
                .isEqualTo(RequirementMatchMode.SINGLE);

        verify(promptBuilder)
                .systemPrompt();

        verify(promptBuilder)
                .userPrompt(cleanedUpDescription);

        verify(llmClient)
                .generate(systemPrompt, userPrompt);

        verify(objectMapper)
                .readValue(
                        llmResponse,
                        LlmRequirementExtractionResponse.class
                );

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

        String cleanedUpDescription =
                JobDescriptionCleaner.clean(jobDescription);

        when(promptBuilder.systemPrompt())
                .thenReturn("system");

        when(promptBuilder.userPrompt(cleanedUpDescription))
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

        verify(llmClient)
                .generate("system", "user");

        verify(objectMapper)
                .readValue(
                        "not valid json",
                        LlmRequirementExtractionResponse.class
                );

        verifyNoInteractions(requirementMapper);
    }
}