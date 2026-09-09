package com.samyak.job_intelligence.job.service.requirement;

import java.util.List;

public record LlmRequirementExtractionResponse(List<LlmExtractedRequirement> requirements) {
}
