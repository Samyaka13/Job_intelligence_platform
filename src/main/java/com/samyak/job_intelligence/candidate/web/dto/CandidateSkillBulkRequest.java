package com.samyak.job_intelligence.candidate.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CandidateSkillBulkRequest(    @NotEmpty
                                            @Valid
                                            List<CandidateSkillCreateRequest> skills) {

}
