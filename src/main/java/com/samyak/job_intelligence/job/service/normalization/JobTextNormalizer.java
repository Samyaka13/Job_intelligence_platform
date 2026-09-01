package com.samyak.job_intelligence.job.service.normalization;

import org.springframework.stereotype.Component;

@Component
public class JobTextNormalizer {

    public String normalizeTitle(String title) {
        return normalizeWhitespaceAndCase(title);
    }

    public String normalizeCompanyName(String companyName) {
        return normalizeWhitespaceAndCase(companyName);
    }

    public String normalizeLocation(String location) {
        return normalizeWhitespaceAndCase(location);
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