package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.job.domain.JobLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobLocationRepository extends JpaRepository<JobLocation,Long> {
    List<JobLocation> findByJobId(Long jobId);
}
