package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobIngestionService {

    private final JobIngestionItemService jobIngestionItemService;
    private final JobSourceListingService jobSourceListingService;

    public JobIngestionService(
            JobIngestionItemService jobIngestionItemService,
            JobSourceListingService jobSourceListingService
    ) {
        this.jobIngestionItemService = jobIngestionItemService;
        this.jobSourceListingService = jobSourceListingService;
    }

    public JobIngestionResult ingest(
            Long companyId,
            String companyName,
            JobSourceCollector jobSourceCollector
    ) {

        List<RawJobListing> listings =
                jobSourceCollector.collect();

        int collected = listings.size();
        int created = 0;
        int updated = 0;
        int failed = 0;

        String sourceCode =
                jobSourceCollector.getSource();

        Set<String> seenExternalJobIds =
                new HashSet<>();

        for (RawJobListing rawJobListing : listings) {

            try {

                if (rawJobListing.externalJobId() != null) {
                    seenExternalJobIds.add(
                            rawJobListing.externalJobId()
                    );
                }

                boolean wasCreated =
                        jobIngestionItemService.ingestListing(
                                companyId,
                                companyName,
                                sourceCode,
                                rawJobListing
                        );

                if (wasCreated) {
                    created++;
                } else {
                    updated++;
                }

            } catch (Exception e) {

                failed++;

                System.out.println(
                        "FAILED JOB: " +
                                rawJobListing.externalJobId()
                );

                e.printStackTrace();
            }
        }

        int deactivated =
                jobSourceListingService.deactivateMissingListings(
                        sourceCode,
                        seenExternalJobIds
                );

        return new JobIngestionResult(
                collected,
                created,
                updated,
                deactivated,
                failed
        );
    }
}