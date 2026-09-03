package com.samyak.job_intelligence.common.normalization;

import org.springframework.stereotype.Component;

@Component
public class TextNormalizationSupport {

    public static String normalizeWhitespaceAndCase(String value) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }
}
