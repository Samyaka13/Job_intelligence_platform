package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobTextNormalizer {

    public String normalizeTitle(String title) {
        return normalizeWhitespaceAndCase(title);
    }

    public String normalizeCompanyName(String companyName) {
        return normalizeWhitespaceAndCase(companyName);
    }


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
                normalizeWhitespaceAndCase(rawJobLocation.city()),
                normalizeWhitespaceAndCase(rawJobLocation.state()),
                normalizeWhitespaceAndCase(rawJobLocation.country()),
                normalizeWhitespaceAndCase(rawJobLocation.displayText())
        )).toList();
    }
    public String normalizeSkill(String skill) {
        return normalizeWhitespaceAndCase(skill);
    }

    public String normalizeJobRequirement(String requirement) {
        return normalizeWhitespaceAndCase(requirement);
    }

    private String normalizeWhitespaceAndCase(String value) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }
}