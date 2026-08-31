package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.service.JobService;

import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.repository.JobSourceListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class JobSourceListingService {
    private final JobSourceListingRepository jobSourceListingRepository;
    private final JobSourceService jobSourceService;
    private final JobService jobService;

    public JobSourceListingService(JobSourceListingRepository jobSourceListingRepository,JobSourceService jobSourceService,JobService jobService){
        this.jobSourceListingRepository = jobSourceListingRepository;
        this.jobSourceService = jobSourceService;
        this.jobService = jobService;
    }

    @Transactional
    public JobSourceListing createOrRefresh(Long jobId, String sourceCode, String externalJobId, String sourceUrl, JsonNode rawPayload, Instant sourcePostedAt){
        Job job = jobService.getById(jobId);
        JobSource jobSource = jobSourceService.getByCode(sourceCode);

        if(externalJobId != null){
            return jobSourceListingRepository.findByJobSourceIdAndExternalJobId(jobSource.getId(),externalJobId).
                    map(existingListing -> {
                                existingListing.refresh(sourceUrl, rawPayload, sourcePostedAt);
                                return existingListing;
                            }
                    ).orElseGet(() -> {
                        JobSourceListing jobSourceListing = new JobSourceListing(job,jobSource,externalJobId,sourceUrl,rawPayload,sourcePostedAt);
                        return jobSourceListingRepository.save(jobSourceListing);
                    }
                    );
        }

        JobSourceListing newListing = new JobSourceListing(job,jobSource, null,sourceUrl,rawPayload,sourcePostedAt);

        return jobSourceListingRepository.save(newListing);
    }

    public JobSourceListing getBySourceAndExternalJobId(String sourceCode ,String externalJobId){
        JobSource jobSource = jobSourceService.getByCode(sourceCode);
        return jobSourceListingRepository.findByJobSourceIdAndExternalJobId(jobSource.getId(),externalJobId).orElseThrow(() -> new IllegalArgumentException("Job source listing not found for source "
                + jobSource.getCode()
                + " and external job ID "
                + externalJobId));

    }
}
