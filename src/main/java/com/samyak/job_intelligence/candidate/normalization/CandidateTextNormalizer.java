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
                .map(TextNormalizationSupport :: normalizeWhitespaceAndCase)
                .filter(location -> location != null && !location.isBlank())
                .toList();
    }
}
