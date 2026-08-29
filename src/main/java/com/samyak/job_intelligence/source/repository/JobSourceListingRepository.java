package com.samyak.job_intelligence.source.repository;

import com.samyak.job_intelligence.source.domain.JobSourceListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobSourceListingRepository extends JpaRepository<JobSourceListing,Long> {
    Optional<JobSourceListing> findByJobSourceIdAndExternalJobId(Long jobSourceId,String externalJobId);

    boolean existsByJobSourceIdAndExternalJobId(Long jobSourceId,String externalJobId);
}
