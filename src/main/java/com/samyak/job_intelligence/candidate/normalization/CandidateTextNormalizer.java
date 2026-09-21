package com.samyak.job_intelligence.candidate.normalization;

import org.springframework.stereotype.Component;
import com.samyak.job_intelligence.common.normalization.TextNormalizationSupport;

import java.util.List;

@Component
public class CandidateTextNormalizer {

    public List<String> normalizeLocations(List<String> locations) {
        if (locations == null) {
            return List.of();
        }

        return locations.stream()
                .map(this::normalizeLocation)
                .filter(location -> location != null && !location.isBlank())
                .toList();
    }

    private String normalizeLocation(String location) {
        if (location == null) {
            return null;
        }

        String normalized =
                TextNormalizationSupport.normalizeWhitespaceAndCase(location);

        if (normalized.isBlank()) {
            return normalized;
        }

        return normalized
                .replace(",", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public String normalizeSkill(String skill) {
        if (skill == null) {
            return null;
        }

        return TextNormalizationSupport.normalizeWhitespaceAndCase(skill);
    }
}