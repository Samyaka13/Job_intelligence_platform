package com.samyak.job_intelligence.candidate.web;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.service.CandidateSkillService;
import com.samyak.job_intelligence.candidate.web.dto.CandidateSkillCreateRequest;
import com.samyak.job_intelligence.candidate.web.dto.CandidateSkillResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CandidateSkillController {
    private final CandidateSkillService candidateSkillService;


    public CandidateSkillController(CandidateSkillService candidateSkillService) {
        this.candidateSkillService = candidateSkillService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    public CandidateSkillResponse addSkill(@PathVariable Long candidateProfileId ,
                                           @Valid @RequestBody CandidateSkillCreateRequest request){
        CandidateSkill candidateSkill = candidateSkillService.addSkill(
                candidateProfileId,
                request.skill(),
                request.normalizedSkill(),
                request.proficiency(),
                request.yearsExperience(),
                request.primary());
        return  CandidateSkillResponse.from(candidateSkill);
    }
}
