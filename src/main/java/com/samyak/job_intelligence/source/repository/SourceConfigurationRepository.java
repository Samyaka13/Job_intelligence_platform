package com.samyak.job_intelligence.source.repository;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceConfigurationRepository extends JpaRepository<SourceConfiguration,Long> {
    List<SourceConfiguration> findByCompanyIdAndEnabledTrue(Long companyId);
    Optional<SourceConfiguration> findByCompanyIdAndJobSourceId(Long companyId,Long jobSourceId);
}
