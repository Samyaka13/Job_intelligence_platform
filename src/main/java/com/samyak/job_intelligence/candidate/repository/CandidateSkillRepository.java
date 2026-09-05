package com.samyak.job_intelligence.candidate.repository;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill,Long> {

    List<CandidateSkill> findByCandidateProfileId(Long candidateProfileId);

    List<CandidateSkill> findByCandidateProfile_IdAndNormalizedSkillIn(
            Long candidateProfileId,
            List<String> normalizedSkills
    );
}
