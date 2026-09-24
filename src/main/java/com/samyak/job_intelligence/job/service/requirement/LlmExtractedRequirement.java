package com.samyak.job_intelligence.job.service.requirement;

import com.samyak.job_intelligence.job.domain.RequirementMatchMode;
import com.samyak.job_intelligence.job.domain.RequirementType;

import java.math.BigDecimal;
import java.util.List;

public record LlmExtractedRequirement(RequirementType requirementType,
                                      List<String> values,
                                      boolean mandatory,
                                      BigDecimal yearsRequired,
                                      String requirementText,
                                      RequirementMatchMode requirementMatchMode
                                      ) { }
