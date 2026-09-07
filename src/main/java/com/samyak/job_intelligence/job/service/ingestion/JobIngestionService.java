package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class JobIngestionService {
    private final JobNormalizer jobNormalizer;
    private final JobService jobService;
    private final JobSourceListingService jobSourceListingService;
    private final JobLocationService jobLocationService;


    public JobIngestionService(JobNormalizer jobNormalizer, JobService jobService, JobSourceListingService jobSourceListingService,JobLocationService jobLocationService) {
        this.jobNormalizer = jobNormalizer;
        this.jobService = jobService;
        this.jobSourceListingService = jobSourceListingService;
        this.jobLocationService = jobLocationService;
    }

    public void ingest(Long companyId, String companyName, JobSourceCollector jobSourceCollector){
        List<RawJobListing> listings = jobSourceCollector.collect();

        for(RawJobListing  rawJobListing : listings){
            ingestListing(companyId,companyName,jobSourceCollector.getSource(),rawJobListing);
        }
    }

    public void ingestListing(Long companyId,String companyName,String sourceCode,RawJobListing rawJobListing){
        NormalizedJobData normalizedJobData = jobNormalizer.normalize(rawJobListing,companyName);
        Job job = jobService.findByCanonicalFingerPrint((normalizedJobData.canonicalFingerprint()));

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
        }else{
            job.markSeen(Instant.now());
        }

        jobSourceListingService.createOrRefresh(job.getId(),sourceCode,rawJobListing.externalJobId(),normalizedJobData.normalizedSourceUrl(), rawJobListing.rawPayload(), rawJobListing.postedAt());
        jobLocationService.replaceLocations(job.getId(),normalizedJobData.locations());
    }
}
