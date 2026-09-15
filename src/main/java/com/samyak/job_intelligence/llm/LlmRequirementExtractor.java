package com.samyak.job_intelligence.llm;

import com.samyak.job_intelligence.job.service.requirement.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class LlmRequirementExtractor implements RequirementExtractor {

    private final LlmClient llmClient;
    private final LlmRequirementPromptBuilder llmRequirementPromptBuilder;
    private final LlmRequirementMapper llmRequirementMapper;
    private final ObjectMapper objectMapper;
    private final JobDescriptionCleaner jobDescriptionCleaner;

    public LlmRequirementExtractor(LlmClient llmClient, LlmRequirementPromptBuilder llmRequirementPromptBuilder, LlmRequirementMapper llmRequirementMapper, ObjectMapper objectMapper, JobDescriptionCleaner jobDescriptionCleaner) {
        this.llmClient = llmClient;
        this.llmRequirementPromptBuilder = llmRequirementPromptBuilder;
        this.llmRequirementMapper = llmRequirementMapper;
        this.objectMapper = objectMapper;
        this.jobDescriptionCleaner = jobDescriptionCleaner;
    }


    @Override
    public List<ExtractedJobRequirement> extract(String jobDescription) {
        if(jobDescription == null || jobDescription.isBlank()) return List.of();

        String systemPrompt  = llmRequirementPromptBuilder.systemPrompt();

        String cleanedDesc = jobDescriptionCleaner.clean(jobDescription);

        // I am writing this to remove the HTML thing from the JD coming from GREENHOUSE To reduce the token cost of input
        String userPrompt = llmRequirementPromptBuilder.userPrompt(cleanedDesc);

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
