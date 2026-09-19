package com.samyak.job_intelligence.source.repository;

import com.samyak.job_intelligence.source.domain.JobSourceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobSourceListingRepository extends JpaRepository<JobSourceListing,Long> {
    Optional<JobSourceListing> findByJobSourceIdAndExternalJobId(Long jobSourceId,String externalJobId);

    boolean existsByJobSourceIdAndExternalJobId(Long jobSourceId,String externalJobId);

    List<JobSourceListing> findByJob_Company_IdAndJobSource_IdAndIsActiveTrue(
            Long companyId,
            Long jobSourceId
    );

    //This query is written to fix the polluted data that happened because of incorrect canonical fingerprint generation
    @Query("""
            SELECT jsl
            FROM JobSourceListing jsl
            JOIN FETCH jsl.job
            JOIN FETCH jsl.jobSource
            WHERE jsl.jobSource.id = :jobSourceId
            ORDER BY jsl.job.id, jsl.id
            """)
    List<JobSourceListing> findAllForRepair(Long jobSourceId);
}
