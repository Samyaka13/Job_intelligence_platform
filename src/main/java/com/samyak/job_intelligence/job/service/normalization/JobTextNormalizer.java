package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.common.normalization.TextNormalizationSupport;
import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobTextNormalizer {

    public String normalizeDescription(String description){
        if (description == null) {
            return null;
        }

        return description
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
    public List<NormalizedJobLocation> normalizeJobLocations(List<RawJobLocation> rawJobLocations){
        if(rawJobLocations == null) return List.of();
        return rawJobLocations.stream().map(rawJobLocation -> new NormalizedJobLocation(
                TextNormalizationSupport.normalizeWhitespaceAndCase(rawJobLocation.city()),
                TextNormalizationSupport.normalizeWhitespaceAndCase(rawJobLocation.state()),
                TextNormalizationSupport.normalizeWhitespaceAndCase(rawJobLocation.country()),
                TextNormalizationSupport.normalizeWhitespaceAndCase(rawJobLocation.displayText())
        )).toList();
    }
    public String normalizeSkill(String skill) {
        return TextNormalizationSupport.normalizeWhitespaceAndCase(skill);
    }

    public String normalizeJobRequirement(String requirement) {
        return TextNormalizationSupport.normalizeWhitespaceAndCase(requirement);
    }


}