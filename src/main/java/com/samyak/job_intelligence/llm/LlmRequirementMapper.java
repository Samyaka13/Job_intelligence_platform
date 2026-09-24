package com.samyak.job_intelligence.llm;

import com.samyak.job_intelligence.common.normalization.TextNormalizationSupport;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.LlmExtractedRequirement;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class LlmRequirementMapper {
    public List<ExtractedJobRequirement> map(List<LlmExtractedRequirement> llmExtractedRequirements){
        if(llmExtractedRequirements == null ||llmExtractedRequirements.isEmpty()) return List.of();

        return llmExtractedRequirements.stream().flatMap((requirement) -> mapSingle(requirement).stream()).toList();
    }

    private List<ExtractedJobRequirement> mapSingle(LlmExtractedRequirement llmExtractedRequirement){
        validate(llmExtractedRequirement);
        String groupId = generateGroupId(llmExtractedRequirement);
        return llmExtractedRequirement.values()
                .stream()
                .map(String ::trim)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    String normalizedValue = TextNormalizationSupport.normalizeWhitespaceAndCase(value);
                    return new ExtractedJobRequirement(
                            llmExtractedRequirement.requirementType(),
                            value,
                            normalizedValue,
                            llmExtractedRequirement.mandatory(),
                            llmExtractedRequirement.yearsRequired(),
                            llmExtractedRequirement.requirementText(),
                            groupId,
                            llmExtractedRequirement.requirementMatchMode()
                    );
                })
                .toList();
    }

    private void validate(LlmExtractedRequirement llmExtractedRequirement){
        if(llmExtractedRequirement == null)  throw new IllegalArgumentException(
                "LLM requirement cannot be null"
        );

        if(llmExtractedRequirement.requirementType() == null){
            throw new IllegalArgumentException("Requirement type cannot be null");
        }


        if (llmExtractedRequirement.values() == null ||
                llmExtractedRequirement.values().isEmpty()) {
            throw new IllegalArgumentException(
                    "Requirement value cannot be empty"
            );
        }
        if (llmExtractedRequirement.values().stream()
                .anyMatch(value ->
                        value == null || value.isBlank())) {
            throw new IllegalArgumentException(
                    "Requirement values cannot contain blank values"
            );
        }

        if (llmExtractedRequirement.yearsRequired() != null &&
                llmExtractedRequirement.yearsRequired().signum() < 0) {
            throw new IllegalArgumentException(
                    "Required years cannot be negative"
            );
        }
        if (llmExtractedRequirement.requirementMatchMode() == null) {
            throw new IllegalArgumentException(
                    "Requirement match mode cannot be null"
            );
        }
    }

    private String generateGroupId(
            LlmExtractedRequirement requirement
    ) {
        String groupInput =
                requirement.requirementType().name()
                        + "|"
                        + requirement.requirementMatchMode().name()
                        + "|"
                        + requirement.requirementText();

        return UUID.nameUUIDFromBytes(
                groupInput.getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}
