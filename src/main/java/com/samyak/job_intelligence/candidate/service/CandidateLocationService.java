package com.samyak.job_intelligence.candidate.service;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.normalization.CandidateTextNormalizer;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Service
public class CandidateLocationService {
    private final CandidateTextNormalizer candidateTextNormalizer ;

    public CandidateLocationService(CandidateTextNormalizer candidateTextNormalizer) {
        this.candidateTextNormalizer = candidateTextNormalizer;
    }

    public List<String> getPreferredLocations(CandidateProfile candidateProfile){
        JsonNode preferredLocations = candidateProfile.getPreferredLocations();

        if(preferredLocations == null || !preferredLocations.isArray()) return  List.of();

        List<String> locations = preferredLocations
                .valueStream()
                .filter(JsonNode :: isString)
                .map(JsonNode :: asString)
                .map(String :: trim)
                .filter(location -> !location.isBlank())
                .toList();

        return  candidateTextNormalizer.normalizeLocations(locations);

    }

}
