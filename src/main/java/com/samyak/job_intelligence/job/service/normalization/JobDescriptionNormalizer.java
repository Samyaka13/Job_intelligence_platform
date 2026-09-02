package com.samyak.job_intelligence.job.service.normalization;

import org.springframework.stereotype.Component;

@Component
public class JobDescriptionNormalizer {

    public String normalize(String description) {
        if (description == null) {
            return null;
        }

        return description
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}