package com.samyak.job_intelligence.job.service.requirement;

import java.util.List;

public interface RequirementExtractor {
    List<ExtractedJobRequirement> extract(String jobDescription);
}
