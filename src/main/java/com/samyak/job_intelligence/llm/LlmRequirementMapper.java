package com.samyak.job_intelligence.llm;

import com.samyak.job_intelligence.common.normalization.TextNormalizationSupport;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.LlmExtractedRequirement;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlmRequirementMapper {
    public List<ExtractedJobRequirement> map(List<LlmExtractedRequirement> llmExtractedRequirements){
        if(llmExtractedRequirements == null ||llmExtractedRequirements.isEmpty()) return List.of();

        return llmExtractedRequirements.stream().map(this::mapSingle).toList();
    }

    private ExtractedJobRequirement mapSingle(LlmExtractedRequirement llmExtractedRequirement){
        validate(llmExtractedRequirement);
        String normalizedValue = TextNormalizationSupport.normalizeWhitespaceAndCase(llmExtractedRequirement.value());
        return new ExtractedJobRequirement(
                llmExtractedRequirement.requirementType(),
                llmExtractedRequirement.value().trim(),
                normalizedValue,
                llmExtractedRequirement.mandatory(),
                llmExtractedRequirement.yearsRequired(),
                llmExtractedRequirement.requirementText()
        );
    }

    private void validate(LlmExtractedRequirement llmExtractedRequirement){
        if(llmExtractedRequirement == null)  throw new IllegalArgumentException(
                "LLM requirement cannot be null"
        );

        if(llmExtractedRequirement.requirementType() == null){
            throw new IllegalArgumentException("Requirement type cannot be null");
        }


        if (llmExtractedRequirement.value() == null ||
                llmExtractedRequirement.value().isBlank()) {
            throw new IllegalArgumentException(
                    "Requirement value cannot be blank"
            );
        }

        if (llmExtractedRequirement.yearsRequired() != null &&
                llmExtractedRequirement.yearsRequired().signum() < 0) {
            throw new IllegalArgumentException(
                    "Required years cannot be negative"
            );
        }
    }
}
