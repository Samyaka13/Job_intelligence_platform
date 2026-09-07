package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.job.domain.RequirementType;

import java.math.BigDecimal;

public record ExtractedJobRequirement(RequirementType requirementType,
                                      String value,
                                      String normalizedValue,
                                      boolean mandatory,
                                      BigDecimal yearsRequired,
                                      String requirementText) {
}
