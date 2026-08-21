package com.samyak.job_intelligence.company.repository;

import com.samyak.job_intelligence.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByCanonicalName(String canonicalName);
    boolean existsByCanonicalName(String canonicalName);

}
