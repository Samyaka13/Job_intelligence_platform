package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.job.domain.JobMatch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobMatchRepository extends JpaRepository<JobMatch,Long> {
    Optional<JobMatch> findByJobIdAndCandidateProfileId(Long jobId,Long candidateProfileId);
    boolean existsByJobIdAndCandidateProfileId(Long jobId,Long candidateProfileId);
    @EntityGraph(attributePaths = {"job", "job.company"})
    @Query("""
            SELECT jm
            FROM JobMatch jm
            WHERE jm.candidateProfile.id = :candidateProfileId
              AND jm.hardQualified = true
              AND jm.job.status = com.samyak.job_intelligence.job.domain.JobStatus.ACTIVE
              AND EXISTS (
                  SELECT 1
                  FROM JobSourceListing jsl
                  WHERE jsl.job = jm.job
                    AND jsl.isActive = true
              )
            ORDER BY jm.finalScore DESC, jm.evaluatedAt DESC
            """)
    List<JobMatch> findCurrentlyEligibleMatches(
            @Param("candidateProfileId") Long candidateProfileId
    );
}
