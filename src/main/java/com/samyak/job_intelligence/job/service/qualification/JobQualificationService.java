package com.samyak.job_intelligence.job.service.qualification;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.normalization.CandidateTextNormalizer;
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
    private final CandidateTextNormalizer candidateTextNormalizer;

    public JobQualificationService(CandidateLocationService candidateLocationService,
                                   CandidateEmploymentTypeService candidateEmploymentTypeService,
                                   CandidateTextNormalizer candidateTextNormalizer) {
        this.candidateLocationService = candidateLocationService;
        this.candidateEmploymentTypeService = candidateEmploymentTypeService;
        this.candidateTextNormalizer = candidateTextNormalizer;
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

        if (job.getExperienceMaxYears() != null
                && candidateProfile.getExperienceYears() != null
                && candidateProfile.getExperienceYears()
                .compareTo(job.getExperienceMaxYears()) > 0) {

            rejectionReasons.add(
                    "Candidate experience exceeds job maximum experience"
            );
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
            List<JobLocation> locations,
            List<String> preferredLocations
    ) {

        if (locations == null || locations.isEmpty()) {
            return true;
        }

        List<JobLocation> knownLocations = locations.stream()
                .filter(this::hasKnownLocation)
                .toList();

        // We cannot determine a mismatch when every job location is unknown.
        if (knownLocations.isEmpty()) {
            return true;
        }

        return knownLocations.stream()
                .anyMatch(jobLocation ->
                        preferredLocations.stream()
                                .anyMatch(preferredLocation ->
                                        locationsMatch(jobLocation, preferredLocation)
                                )
                );
    }


    private boolean hasKnownLocation(JobLocation location) {

        if (location == null) {
            return false;
        }

        return isNotBlank(location.getCity())
                || isNotBlank(location.getState())
                || isNotBlank(location.getCountry())
                || isNotBlank(location.getDisplayText());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean locationsMatch(
            JobLocation jobLocation,
            String preferredLocation
    ) {

        String jobLocationValue = jobLocation.getCity();

        if (jobLocationValue == null || jobLocationValue.isBlank()) {
            return false;
        }

        return jobLocationValue.equals(preferredLocation)
                || jobLocationValue.startsWith(preferredLocation + " ")
                || preferredLocation.startsWith(jobLocationValue + " ");
    }


}
