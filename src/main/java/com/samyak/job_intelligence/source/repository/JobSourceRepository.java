package com.samyak.job_intelligence.source.repository;


import com.samyak.job_intelligence.source.domain.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobSourceRepository extends JpaRepository<JobSource,Long> {
    Optional<JobSource> findByCode(String code);
    boolean existsByCode(String code);
}
