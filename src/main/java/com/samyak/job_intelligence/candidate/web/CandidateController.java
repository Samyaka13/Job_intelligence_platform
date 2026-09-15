package com.samyak.job_intelligence.candidate.web;


import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.service.CandidateProfileService;
import com.samyak.job_intelligence.candidate.web.dto.CandidateCreateRequest;
import com.samyak.job_intelligence.candidate.web.dto.CandidateResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {
    private final CandidateProfileService candidateProfileService;

    public CandidateController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CandidateResponse create(
            @Valid @RequestBody
            CandidateCreateRequest request){
        CandidateProfile candidateProfile = candidateProfileService.create(
                request.name(),
                request.email(),
                request.currentLocation(),
                request.preferredLocations(),
                request.minimumSalary(),
                request.minimumSalaryCurrency(),
                request.experienceYears(),
                request.preferredEmploymentTypes()

        );
        return CandidateResponse.from(candidateProfile);
    }

}
