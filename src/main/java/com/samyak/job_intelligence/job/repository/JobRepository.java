package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job,Long> {
    List<Job> findAllByCanonicalFingerprint(String canonicalFingerprint);
    Optional<Job> findByCanonicalApplicationUrl(String canonicalApplicationUrl);

    @Query("""
        SELECT j
        FROM Job j
        WHERE j.status = :status
          AND EXISTS (
              SELECT 1
              FROM JobSourceListing jsl
              WHERE jsl.job = j
                AND jsl.isActive = true
          )
        ORDER BY j.postedAt DESC
        """)
    List<Job> findAvailableByStatusOrderByPostedAtDesc(
            @Param("status") JobStatus jobStatus
    );
}
