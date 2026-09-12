package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementExtractionService;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementService;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class JobIngestionService {
    private final JobNormalizer jobNormalizer;
    private final JobService jobService;
    private final JobSourceListingService jobSourceListingService;
    private final JobLocationService jobLocationService;
    private final JobRequirementExtractionService jobRequirementExtractionService;
    private final JobRequirementService jobRequirementService;


    public JobIngestionService(JobNormalizer jobNormalizer, JobService jobService, JobSourceListingService jobSourceListingService,JobLocationService jobLocationService,JobRequirementExtractionService jobRequirementExtractionService,JobRequirementService jobRequirementService) {
        this.jobNormalizer = jobNormalizer;
        this.jobService = jobService;
        this.jobSourceListingService = jobSourceListingService;
        this.jobLocationService = jobLocationService;
        this.jobRequirementExtractionService = jobRequirementExtractionService;
        this.jobRequirementService = jobRequirementService;
    }

    public JobIngestionResult ingest(Long companyId, String companyName, JobSourceCollector jobSourceCollector){
        List<RawJobListing> listings = jobSourceCollector.collect();
        int collected = listings.size();
        int created = 0;
        int updated = 0;
        int deactivated = 0;
        int failed = 0;
        String sourceCode = jobSourceCollector.getSource();
        Set<String> seenExternalJobIds = new HashSet<>();

        for(RawJobListing rawJobListing : listings){
           try{
                if (rawJobListing.externalJobId() != null) {
                    seenExternalJobIds.add(rawJobListing.externalJobId());
                }
                boolean wasCreated = ingestListing(companyId, companyName, sourceCode, rawJobListing);
                if (wasCreated) created++;
                else updated++;
            }catch (Exception e){
               failed++;
           }
        }
        deactivated = jobSourceListingService.deactivateMissingListings(sourceCode,seenExternalJobIds);

        return new JobIngestionResult(collected,created,updated,deactivated,failed);
    }

    public boolean ingestListing(Long companyId,String companyName,String sourceCode,RawJobListing rawJobListing){
        NormalizedJobData normalizedJobData = jobNormalizer.normalize(rawJobListing,companyName);
        Job job = jobService.findByCanonicalFingerPrint((normalizedJobData.canonicalFingerprint()));
        boolean created = false;

        if(job == null){
            job = jobService.create(
                    companyId,
                    normalizedJobData.title(),
                    normalizedJobData.normalizedTitle(),
                    normalizedJobData.description(),
                    normalizedJobData.employmentType(),
                    normalizedJobData.seniorityLevel(),
                    normalizedJobData.experienceMinYears(),
                    normalizedJobData.experienceMaxYears(),
                    normalizedJobData.salaryMin(),
                    normalizedJobData.salaryMax(),
                    normalizedJobData.salaryCurrency(),
                    normalizedJobData.postedAt(),
                    null,
                    normalizedJobData.normalisedApplicationUrl(),
                    normalizedJobData.canonicalFingerprint(),
                    normalizedJobData.descriptionHash()
            );
            created = true;
        }else{
            job.markSeen(Instant.now());
        }

        jobSourceListingService.createOrRefresh(job.getId(),sourceCode,rawJobListing.externalJobId(),normalizedJobData.normalizedSourceUrl(), rawJobListing.rawPayload(), rawJobListing.postedAt());
        jobLocationService.replaceLocations(job.getId(),normalizedJobData.locations());

        List<ExtractedJobRequirement> requirements = jobRequirementExtractionService.extract(normalizedJobData.description());
        jobRequirementService.replaceRequirements(job.getId(),requirements);
        return created;
    }
}
