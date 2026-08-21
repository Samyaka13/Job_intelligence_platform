package com.samyak.job_intelligence.company.web.dto;

import com.samyak.job_intelligence.company.domain.Company;

import java.time.Instant;

public record CompanyResponse(Long id, String canonicalName, String displayName, String websiteUrl, Instant createdAt,Instant updatedAt) {
    public static CompanyResponse from(Company company){
        return new CompanyResponse(company.getId(),
                company.getCanonicalName(),
                company.getDisplayName(),
                company.getWebsiteUrl(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
