package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record NormalizedJobData(
        String companyName,
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
                                BigDecimal experienceMinYears,
                                BigDecimal experienceMaxYears,
                                BigDecimal salaryMin,
                                BigDecimal salaryMax,
                                String salaryCurrency,
                                List<NormalizedJobLocation> locations,
                                Instant postedAt
) {

}
