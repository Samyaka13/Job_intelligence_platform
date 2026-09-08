package com.samyak.job_intelligence.job.service.requirement;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class LlmRequirementExtractor implements RequirementExtractor{

    private final LlmClient llmClient;
    private final LlmRequirementPromptBuilder llmRequirementPromptBuilder;
    private final LlmRequirementMapper llmRequirementMapper;
    private final ObjectMapper objectMapper;

    public LlmRequirementExtractor(LlmClient llmClient, LlmRequirementPromptBuilder llmRequirementPromptBuilder, LlmRequirementMapper llmRequirementMapper, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.llmRequirementPromptBuilder = llmRequirementPromptBuilder;
        this.llmRequirementMapper = llmRequirementMapper;
        this.objectMapper = objectMapper;
    }


    @Override
    public List<ExtractedJobRequirement> extract(String jobDescription) {
        if(jobDescription == null || jobDescription.isBlank()) return List.of();

        String systemPrompt  = llmRequirementPromptBuilder.systemPrompt();

        String userPrompt = llmRequirementPromptBuilder.userPrompt(jobDescription);

        String response = llmClient.generate(systemPrompt,userPrompt);


        try {
            LlmRequirementExtractionResponse extractionResponse = objectMapper.
                    readValue(response,LlmRequirementExtractionResponse.class);

            if(extractionResponse == null || extractionResponse.requirements() == null){
                return List.of();
            }

            return llmRequirementMapper.map(extractionResponse.requirements());

        }catch (Exception exception){
            throw new IllegalArgumentException(
                    "Failed to parse LLM requirement extraction response",
                    exception
            );
        }

    }
}
