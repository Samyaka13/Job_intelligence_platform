package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Component;

@Component
public class JobNormalizer {
    private final JobTextNormalizer jobTextNormalizer;
    private final JobUrlNormalizer jobUrlNormalizer;
    private final JobDescriptionNormalizer jobDescriptionNormalizer;
    private final DescriptionHashGenerator descriptionHashGenerator;

    public JobNormalizer(JobTextNormalizer jobTextNormalizer, JobUrlNormalizer jobUrlNormalizer, JobDescriptionNormalizer jobDescriptionNormalizer, DescriptionHashGenerator descriptionHashGenerator) {
        this.jobTextNormalizer = jobTextNormalizer;
        this.jobUrlNormalizer = jobUrlNormalizer;
        this.jobDescriptionNormalizer = jobDescriptionNormalizer;
        this.descriptionHashGenerator = descriptionHashGenerator;
    }

    public NormalizedJobData normalize(RawJobListing rawJobListing,String companyName){
        String normalizedCompanyName = jobTextNormalizer.normalizeCompanyName(companyName);
        String normalizedTitle = jobTextNormalizer.normalizeTitle(rawJobListing.title());
        String normalizedDescription = jobDescriptionNormalizer.normalize(rawJobListing.description());
        String normalizedSourceUrl = jobUrlNormalizer.normalize(rawJobListing.sourceUrl());
        String normalizedApplicationUrl = jobUrlNormalizer.normalize(rawJobListing.applicationUrl());
        String descriptionHash = descriptionHashGenerator.generate(normalizedDescription);
        return new NormalizedJobData(companyName,
                normalizedCompanyName,
                rawJobListing.title(),
                normalizedTitle,
                normalizedDescription,
                descriptionHash,
                rawJobListing.sourceUrl(),
                normalizedSourceUrl,
                rawJobListing.applicationUrl(),
                normalizedApplicationUrl,
                EmploymentType.UNKNOWN,
                SeniorityLevel.UNKNOWN,
                rawJobListing.postedAt());
    }
}
