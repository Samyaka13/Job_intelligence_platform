package com.samyak.job_intelligence.candidate.service;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.exception.CandidateSkillAlreadyExistsException;
import com.samyak.job_intelligence.candidate.normalization.CandidateTextNormalizer;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CandidateSkillService {
    private final CandidateSkillRepository candidateSkillRepository;
    private final CandidateProfileService candidateProfileService;
    private final CandidateTextNormalizer candidateTextNormalizer;

    public CandidateSkillService(CandidateSkillRepository candidateSkillRepository, CandidateProfileService candidateProfileService, CandidateTextNormalizer candidateTextNormalizer) {
        this.candidateSkillRepository = candidateSkillRepository;
        this.candidateProfileService = candidateProfileService;
        this.candidateTextNormalizer = candidateTextNormalizer;
    }


    @Transactional
    public CandidateSkill addSkill(
            Long candidateProfileId,
            String skill,
            String proficiency,
            BigDecimal yearsExperience,
            boolean primary
    ){
        CandidateProfile candidateProfile = candidateProfileService.getById(candidateProfileId);
        String normalizedSkill =
                candidateTextNormalizer.normalizeSkill(skill);
        if(candidateSkillRepository.existsByCandidateProfileIdAndNormalizedSkill(candidateProfileId,normalizedSkill)){
                throw new CandidateSkillAlreadyExistsException(
                        "Candidate already has skill: " + skill
                );
        }
        CandidateSkill candidateSkill = new CandidateSkill(
                candidateProfile,
                skill,
                normalizedSkill,
                proficiency,
                yearsExperience,
                primary
        );

        return candidateSkillRepository.save(candidateSkill);
    }
}
