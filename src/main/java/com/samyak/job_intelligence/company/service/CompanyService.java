package com.samyak.job_intelligence.company.service;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CompanyService {
    private final CompanyRepository companyRepository;
    public CompanyService(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company create(String canonicalName,String displayName,String websiteUrl){
        String normalizedCanonicalName = canonicalName.trim().toLowerCase();
        if(companyRepository.existsByCanonicalName(normalizedCanonicalName)){
            throw new IllegalArgumentException(  "Company already exists: " + normalizedCanonicalName);
        }
        Company company = new Company(normalizedCanonicalName,displayName.trim(),websiteUrl);

        return companyRepository.save(company);
    }
    public Company getById(Long id){
        return companyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Company Not found : " + id));
    }
}
