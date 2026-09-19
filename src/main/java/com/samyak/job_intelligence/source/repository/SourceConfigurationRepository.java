package com.samyak.job_intelligence.source.repository;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SourceConfigurationRepository extends JpaRepository<SourceConfiguration,Long> {
    @Query("""
    SELECT sc
    FROM SourceConfiguration sc
    JOIN FETCH sc.jobSource
    WHERE sc.company.id = :companyId
      AND sc.enabled = true
""")
    List<SourceConfiguration> findByCompanyIdAndEnabledTrue(Long companyId);
    Optional<SourceConfiguration> findByCompanyIdAndJobSourceId(Long companyId,Long jobSourceId);
}
