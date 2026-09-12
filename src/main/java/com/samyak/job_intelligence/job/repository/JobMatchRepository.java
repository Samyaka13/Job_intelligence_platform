package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.job.domain.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobMatchRepository extends JpaRepository<JobMatch,Long> {
    Optional<JobMatch> findByJobIdAndCandidateProfileId(Long jobId,Long candidateProfileId);
    boolean existsByJobIdAndCandidateProfileId(Long jobId,Long candidateProfileId);

    List<JobMatch> findByCandidateProfileIdAndHardQualifiedTrueOrderByFinalScoreDescEvaluatedAtDesc(Long candidateProfileId);
}
