package com.samyak.job_intelligence.candidate.repository;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill,Long> {

    List<CandidateSkill> findByCandidateProfileId(Long candidateProfileId);

    List<CandidateSkill> findByCandidateProfileAndNormalizedSkillIn(
            Long candidateProfileId,
            List<String> normalizedSkills
    );
}
