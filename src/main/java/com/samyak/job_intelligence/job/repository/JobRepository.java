package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job,Long> {
    Optional<Job> findByCanonicalFingerprint(String canonicalFingerPrint);
    boolean existsByCanonicalFingerprint(String canonicalFingerPrint);
    Optional<Job> findByCanonicalApplicationUrl(String canonicalApplicationUrl);
    List<Job> findByStatusOrderByPostedAtDesc(JobStatus jobStatus);
}
