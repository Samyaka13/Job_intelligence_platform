package com.samyak.job_intelligence.job.service.qualification;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobQualificationService {
    public JobQualificationResult qualify(NormalizedJobData job, CandidateProfile candidateProfile){
        List<String> rejectionReasons = new ArrayList<>();
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
}
