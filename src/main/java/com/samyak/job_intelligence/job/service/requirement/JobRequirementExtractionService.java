package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.llm.LlmRequirementExtractor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobRequirementExtractionService {
    private final RequirementExtractor ruleBasedRequirementExtractor;
    private final RequirementExtractor llmBasedRequirementExtractor;


    public JobRequirementExtractionService(RuleBasedRequirementExtractor ruleBasedRequirementExtractor, LlmRequirementExtractor llmBasedRequirementExtractor) {
        this.ruleBasedRequirementExtractor = ruleBasedRequirementExtractor;
        this.llmBasedRequirementExtractor = llmBasedRequirementExtractor;
    }

    public List<ExtractedJobRequirement> extract(String jobDescription){
        List<ExtractedJobRequirement> ruleResults = ruleBasedRequirementExtractor.extract(jobDescription);
        List<ExtractedJobRequirement> llmResults = llmBasedRequirementExtractor.extract(jobDescription);
        return merge(ruleResults,llmResults);
    }

    private List<ExtractedJobRequirement> merge(List<ExtractedJobRequirement> ruleResults,
                                                List<ExtractedJobRequirement> llmResults){
        Map<String,ExtractedJobRequirement> merged = new LinkedHashMap<>();

        addResults(merged, ruleResults);
        addResults(merged, llmResults);
        return new ArrayList<>(merged.values());

    }

    private void addResults(Map<String, ExtractedJobRequirement> merged,
                            List<ExtractedJobRequirement> results){
        if(results == null) return;

        for(ExtractedJobRequirement requirement : results){
            String key =
                    requirement.normalizedValue();

            merged.put(key, requirement);
        }
    }
}
