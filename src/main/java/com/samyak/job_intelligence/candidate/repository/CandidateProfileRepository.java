package com.samyak.job_intelligence.candidate.repository;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile,Long> {
    Optional<CandidateProfile> findByEmail(String email);
    boolean existsByEmail(String email);
}
