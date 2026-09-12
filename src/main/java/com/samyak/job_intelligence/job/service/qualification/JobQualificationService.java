package com.samyak.job_intelligence.job.service.qualification;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.service.CandidateEmploymentTypeService;
import com.samyak.job_intelligence.candidate.service.CandidateLocationService;
import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobLocation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobQualificationService {
    private final CandidateLocationService candidateLocationService;
    private final CandidateEmploymentTypeService candidateEmploymentTypeService;

    public JobQualificationService(CandidateLocationService candidateLocationService,CandidateEmploymentTypeService candidateEmploymentTypeService) {
        this.candidateLocationService = candidateLocationService;
        this.candidateEmploymentTypeService = candidateEmploymentTypeService;
    }



    public JobQualificationResult qualify(Job job, CandidateProfile candidateProfile, List<JobLocation> locations){
        List<String> rejectionReasons = new ArrayList<>();
        List<String> preferredLocations = candidateLocationService.getPreferredLocations(candidateProfile);
        List<EmploymentType> preferredEmploymentType = candidateEmploymentTypeService.getPreferredEmploymentTypes(candidateProfile);
        if(!preferredLocations.isEmpty() && !hasMatchingLocation(locations,preferredLocations)){
            rejectionReasons.add("Job location does not match candidate preferred locations");
        }
        if(job.getExperienceMinYears() != null && candidateProfile.getExperienceYears() != null && job.getExperienceMinYears().compareTo(candidateProfile.getExperienceYears()) > 0){
            rejectionReasons.add("Required experience exceeds candidate experience");
        }

        if(!preferredEmploymentType.isEmpty() && job.getEmploymentType() != EmploymentType.UNKNOWN && !preferredEmploymentType.contains(job.getEmploymentType())){
            rejectionReasons.add("Job employment type does not match candidate preferences");
        }

        if(job.getSalaryMax() != null &&
                candidateProfile.getMinimumSalary() != null &&
                job.getSalaryCurrency() != null &&
                job.getSalaryCurrency().equalsIgnoreCase(candidateProfile.getMinimumSalaryCurrency()) &&
                job.getSalaryMax().compareTo(candidateProfile.getMinimumSalary()) < 0){
            rejectionReasons.add("Job maximum salary is below candidate minimum salary");
        }
        if(rejectionReasons.isEmpty()) return JobQualificationResult.qualifiedListing();

        return JobQualificationResult.rejectedListing(rejectionReasons);
    }


    private boolean hasMatchingLocation(
            List<JobLocation> jobLocations,
            List<String> preferredLocations
    ) {
        return jobLocations.stream()
                .flatMap(location ->
                        java.util.stream.Stream.of(
                                location.getCity(),
                                location.getState(),
                                location.getCountry(),
                                location.getDisplayText()
                        )
                )
                .filter(java.util.Objects::nonNull)
                .anyMatch(preferredLocations::contains);
    }


}
