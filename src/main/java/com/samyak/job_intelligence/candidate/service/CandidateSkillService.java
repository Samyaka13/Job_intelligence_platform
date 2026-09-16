package com.samyak.job_intelligence.candidate.service;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.exception.CandidateSkillAlreadyExistsException;
import com.samyak.job_intelligence.candidate.normalization.CandidateTextNormalizer;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.candidate.web.dto.CandidateSkillBulkItemResponse;
import com.samyak.job_intelligence.candidate.web.dto.CandidateSkillBulkResponse;
import com.samyak.job_intelligence.candidate.web.dto.CandidateSkillBulkStatus;
import com.samyak.job_intelligence.candidate.web.dto.CandidateSkillCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    public CandidateSkillBulkResponse addSkills(
            Long candidateProfileId,
            List<CandidateSkillCreateRequest> skills
    ) {
        CandidateProfile candidateProfile =
                candidateProfileService.getById(candidateProfileId);

        int created = 0;
        int duplicates = 0;

        List<CandidateSkillBulkItemResponse> results =
                new ArrayList<>();

        for (CandidateSkillCreateRequest request : skills) {

            String normalizedSkill =
                    candidateTextNormalizer.normalizeSkill(request.skill());

            if (candidateSkillRepository
                    .existsByCandidateProfileIdAndNormalizedSkill(
                            candidateProfileId,
                            normalizedSkill
                    )) {

                duplicates++;

                results.add(
                        new CandidateSkillBulkItemResponse(
                                request.skill(),
                                CandidateSkillBulkStatus.DUPLICATE
                        )
                );

                continue;
            }

            CandidateSkill candidateSkill =
                    new CandidateSkill(
                            candidateProfile,
                            request.skill(),
                            normalizedSkill,
                            request.proficiency(),
                            request.yearsExperience(),
                            request.primary()
                    );

            candidateSkillRepository.save(candidateSkill);

            created++;

            results.add(
                    new CandidateSkillBulkItemResponse(
                            request.skill(),
                            CandidateSkillBulkStatus.CREATED
                    )
            );
        }

        return new CandidateSkillBulkResponse(
                created,
                duplicates,
                results
        );
    }


}
