package com.samyak.job_intelligence.job.service.qualification;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.service.CandidateLocationService;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobLocation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobQualificationService {
    private final CandidateLocationService candidateLocationService;

    public JobQualificationService(CandidateLocationService candidateLocationService) {
        this.candidateLocationService = candidateLocationService;
    }



    public JobQualificationResult qualify(NormalizedJobData job, CandidateProfile candidateProfile){
        List<String> rejectionReasons = new ArrayList<>();
        List<String> preferredLocations = candidateLocationService.getPreferredLocations(candidateProfile);
        if(!preferredLocations.isEmpty() && !hasMatchingLocation(job.locations(),preferredLocations)){
            rejectionReasons.add("Job location does not match candidate preferred locations");
        }
        if(job.experienceMinYears() != null && candidateProfile.getExperienceYears() != null && job.experienceMinYears().compareTo(candidateProfile.getExperienceYears()) > 0){
            rejectionReasons.add("Required experience exceeds candidate experience");
        }
        if(job.salaryMax() != null &&
                candidateProfile.getMinimumSalary() != null &&
                job.salaryCurrency() != null &&
                job.salaryCurrency().equalsIgnoreCase(candidateProfile.getMinimumSalaryCurrency()) &&
                job.salaryMax().compareTo(candidateProfile.getMinimumSalary()) < 0){
            rejectionReasons.add("Job maximum salary is below candidate minimum salary");
        }
        if(rejectionReasons.isEmpty()) return JobQualificationResult.qualifiedListing();

        return JobQualificationResult.rejectedListing(rejectionReasons);
    }


    private boolean hasMatchingLocation(
            List<NormalizedJobLocation> jobLocations,
            List<String> preferredLocations
    ) {
        return jobLocations.stream()
                .flatMap(location ->
                        java.util.stream.Stream.of(
                                location.city(),
                                location.state(),
                                location.country(),
                                location.displayText()
                        )
                )
                .filter(java.util.Objects::nonNull)
                .anyMatch(preferredLocations::contains);
    }


}
