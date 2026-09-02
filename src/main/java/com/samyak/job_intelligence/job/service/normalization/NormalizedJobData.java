package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;

import java.time.Instant;

public record NormalizedJobData(String companyName,
                                String normalizedCompanyName,
                                String title,
                                String normalizedTitle,
                                String description,
                                String descriptionHash,
                                String sourceUrl,
                                String normalizedSourceUrl,
                                String applicationUrl,
                                String normalisedApplicationUrl,
                                EmploymentType employmentType,
                                SeniorityLevel seniorityLevel ,
                                Instant postedAt
) {

}
