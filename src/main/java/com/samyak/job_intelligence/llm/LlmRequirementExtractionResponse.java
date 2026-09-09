package com.samyak.job_intelligence.llm;

import com.samyak.job_intelligence.job.service.requirement.LlmExtractedRequirement;

import java.util.List;

public record LlmRequirementExtractionResponse(List<LlmExtractedRequirement> requirements) {
}
