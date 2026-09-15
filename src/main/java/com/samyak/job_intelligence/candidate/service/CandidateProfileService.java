package com.samyak.job_intelligence.candidate.service;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

@Service
public class CandidateProfileService {
    private final CandidateProfileRepository candidateProfileRepository;


    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
    }

    @Transactional
    public CandidateProfile create(
            String name,
            String email,
            String currentLocation,
            JsonNode preferredLocations,
            BigDecimal minimumSalary,
            String minimumSalaryCurrency,
            BigDecimal experienceYears,
            JsonNode preferredEmploymentTypes
    ){
        if(candidateProfileRepository.existsByEmail(email)){
            throw new IllegalArgumentException(
                    "Candidate already exists: " + email
            );
        }
        CandidateProfile candidateProfile = new CandidateProfile(
                name,
                email,
                currentLocation,
                preferredLocations,
                minimumSalary,
                minimumSalaryCurrency,
                true,
                experienceYears,
                preferredEmploymentTypes
        );
        return candidateProfileRepository.save(candidateProfile);
    }

    @Transactional(readOnly = true)
    public CandidateProfile getById(Long candidateProfileId){
        return candidateProfileRepository.findById(candidateProfileId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate profile not found: "
                                        + candidateProfileId
                        )
                );
    }
}
