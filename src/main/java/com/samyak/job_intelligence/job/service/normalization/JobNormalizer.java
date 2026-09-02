package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.parsing.EmploymentTypeParser;
import com.samyak.job_intelligence.job.service.parsing.ExperienceParser;
import com.samyak.job_intelligence.job.service.parsing.ExperienceRange;
import com.samyak.job_intelligence.job.service.parsing.SeniorityParser;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Component;

@Component
public class JobNormalizer {
    private final JobTextNormalizer jobTextNormalizer;
    private final JobUrlNormalizer jobUrlNormalizer;
    private final JobDescriptionNormalizer jobDescriptionNormalizer;
    private final DescriptionHashGenerator descriptionHashGenerator;
    private final ExperienceParser experienceParser;
    private final EmploymentTypeParser employmentTypeParser;
    private final SeniorityParser seniorityParser;

    public JobNormalizer(JobTextNormalizer jobTextNormalizer, JobUrlNormalizer jobUrlNormalizer, JobDescriptionNormalizer jobDescriptionNormalizer, DescriptionHashGenerator descriptionHashGenerator,ExperienceParser experienceParser,EmploymentTypeParser employmentTypeParser,SeniorityParser seniorityParser) {
        this.jobTextNormalizer = jobTextNormalizer;
        this.jobUrlNormalizer = jobUrlNormalizer;
        this.jobDescriptionNormalizer = jobDescriptionNormalizer;
        this.descriptionHashGenerator = descriptionHashGenerator;
        this.experienceParser = experienceParser;
        this.employmentTypeParser = employmentTypeParser;
        this.seniorityParser = seniorityParser;
    }

    public NormalizedJobData normalize(RawJobListing rawJobListing,String companyName){
        String normalizedCompanyName = jobTextNormalizer.normalizeCompanyName(companyName);
        String normalizedTitle = jobTextNormalizer.normalizeTitle(rawJobListing.title());
        String normalizedDescription = jobDescriptionNormalizer.normalize(rawJobListing.description());
        String normalizedSourceUrl = jobUrlNormalizer.normalize(rawJobListing.sourceUrl());
        String normalizedApplicationUrl = jobUrlNormalizer.normalize(rawJobListing.applicationUrl());
        String descriptionHash = descriptionHashGenerator.generate(normalizedDescription);
        ExperienceRange experienceRange = experienceParser.parse(normalizedDescription);
        EmploymentType employmentType = employmentTypeParser.parse(rawJobListing.title());
        SeniorityLevel seniorityLevel = seniorityParser.parse(rawJobListing.title());
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
                employmentType,
                seniorityLevel,
                experienceRange.minYears(),
                experienceRange.maxYears(),
                rawJobListing.postedAt());
    }
}
