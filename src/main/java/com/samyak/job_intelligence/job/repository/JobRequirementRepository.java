package com.samyak.job_intelligence.job.repository;

import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRequirementRepository extends JpaRepository<JobRequirement,Long> {
    List<JobRequirement> findByJobId(Long jobId);
    List<JobRequirement> findByJobIdAndRequirementType(
            Long jobId,
            RequirementType requirementType
    );

}
