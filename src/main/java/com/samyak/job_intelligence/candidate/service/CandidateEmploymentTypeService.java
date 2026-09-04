package com.samyak.job_intelligence.candidate.service;


import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Service
public class CandidateEmploymentTypeService {

    public List<EmploymentType> getPreferredEmploymentTypes(CandidateProfile candidateProfile){
        JsonNode preferredEmploymentType = candidateProfile.getPreferredEmploymentTypes();

        if(preferredEmploymentType == null || !preferredEmploymentType.isArray()){
            return List.of();
        }

        return preferredEmploymentType.valueStream().filter(JsonNode::isString).map(JsonNode :: asString)
                .map(this::parseEmploymentType)
                .filter(type -> type != EmploymentType.UNKNOWN)
                .toList();
    }


    private EmploymentType parseEmploymentType(String value) {
        try {
            return EmploymentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return EmploymentType.UNKNOWN;
        }
    }

}
