package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.job.domain.RequirementType;

import java.math.BigDecimal;

public record LlmExtractedRequirement(RequirementType requirementType,
                                      String value,
                                      boolean mandatory,
                                      BigDecimal yearsRequired,
                                      String requirementText
                                      ) { }
