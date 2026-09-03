package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.parsing.*;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobNormalizer {
    private final JobTextNormalizer jobTextNormalizer;
    private final JobUrlNormalizer jobUrlNormalizer;
    private final DescriptionHashGenerator descriptionHashGenerator;
    private final ExperienceParser experienceParser;
    private final EmploymentTypeParser employmentTypeParser;
    private final SeniorityParser seniorityParser;
    private final SalaryParser salaryParser;

    public JobNormalizer(JobTextNormalizer jobTextNormalizer, JobUrlNormalizer jobUrlNormalizer,DescriptionHashGenerator descriptionHashGenerator,ExperienceParser experienceParser,EmploymentTypeParser employmentTypeParser,SeniorityParser seniorityParser,SalaryParser salaryParser) {
        this.jobTextNormalizer = jobTextNormalizer;
        this.jobUrlNormalizer = jobUrlNormalizer;
        this.descriptionHashGenerator = descriptionHashGenerator;
        this.experienceParser = experienceParser;
        this.employmentTypeParser = employmentTypeParser;
        this.seniorityParser = seniorityParser;
        this.salaryParser = salaryParser;
    }

    public NormalizedJobData normalize(RawJobListing rawJobListing,String companyName){
        String normalizedCompanyName = jobTextNormalizer.normalizeCompanyName(companyName);
        String normalizedTitle = jobTextNormalizer.normalizeTitle(rawJobListing.title());
        String normalizedDescription = jobTextNormalizer.normalizeDescription(rawJobListing.description());
        String normalizedSourceUrl = jobUrlNormalizer.normalize(rawJobListing.sourceUrl());
        String normalizedApplicationUrl = jobUrlNormalizer.normalize(rawJobListing.applicationUrl());
        String descriptionHash = descriptionHashGenerator.generate(normalizedDescription);
        ExperienceRange experienceRange = experienceParser.parse(normalizedDescription);
        EmploymentType employmentType = employmentTypeParser.parse(rawJobListing.title());
        SeniorityLevel seniorityLevel = seniorityParser.parse(rawJobListing.title());
        SalaryRange salaryRange = salaryParser.parse(normalizedDescription);
        List<NormalizedJobLocation> locations = jobTextNormalizer.normalizeJobLocations(rawJobListing.locations());
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
                salaryRange.min(),
                salaryRange.max(),
                salaryRange.currency(),
                locations,
                rawJobListing.postedAt());
    }
}
