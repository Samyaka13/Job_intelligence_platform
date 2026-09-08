package com.samyak.job_intelligence.job.service.requirement;

import java.util.List;

public class LlmRequirementExtractor implements RequirementExtractor{
    @Override
    public List<ExtractedJobRequirement> extract(String jobDescription) {
        return List.of();
    }
}
