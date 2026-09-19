package com.samyak.job_intelligence.job.service.repair;

import com.samyak.job_intelligence.common.normalization.TextNormalizationSupport;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.normalization.JobFingerprintGenerator;
import com.samyak.job_intelligence.job.service.normalization.JobTextNormalizer;
import com.samyak.job_intelligence.job.service.normalization.JobUrlNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobLocation;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.greehouse.GreenhouseJobMapper;
import com.samyak.job_intelligence.source.repository.JobSourceListingRepository;
import com.samyak.job_intelligence.source.service.JobSourceService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobDataRepairService {

    private static final Logger log =
            LoggerFactory.getLogger(JobDataRepairService.class);

    private final JobSourceListingRepository jobSourceListingRepository;
    private final JobRepository jobRepository;
    private final JobSourceService jobSourceService;
    private final GreenhouseJobMapper greenhouseJobMapper;
    private final JobTextNormalizer jobTextNormalizer;
    private final JobUrlNormalizer jobUrlNormalizer;
    private final JobFingerprintGenerator jobFingerprintGenerator;
    private final JobLocationService jobLocationService;

    public JobDataRepairService(
            JobSourceListingRepository jobSourceListingRepository,
            JobRepository jobRepository,
            JobSourceService jobSourceService,
            GreenhouseJobMapper greenhouseJobMapper,
            JobTextNormalizer jobTextNormalizer,
            JobUrlNormalizer jobUrlNormalizer,
            JobFingerprintGenerator jobFingerprintGenerator,
            JobLocationService jobLocationService
    ) {
        this.jobSourceListingRepository = jobSourceListingRepository;
        this.jobRepository = jobRepository;
        this.jobSourceService = jobSourceService;
        this.greenhouseJobMapper = greenhouseJobMapper;
        this.jobTextNormalizer = jobTextNormalizer;
        this.jobUrlNormalizer = jobUrlNormalizer;
        this.jobFingerprintGenerator = jobFingerprintGenerator;
        this.jobLocationService = jobLocationService;
    }

    @Transactional
    public int repairGreenhouseMergedJobs() {

        var greenhouse =
                jobSourceService.getByCode("GREENHOUSE");

        List<JobSourceListing> listings =
                jobSourceListingRepository.findAllForRepair(
                        greenhouse.getId()
                );

        Map<Long, List<JobSourceListing>> grouped =
                listings.stream()
                        .filter(listing ->
                                listing.getExternalJobId() != null
                        )
                        .collect(Collectors.groupingBy(
                                listing -> listing.getJob().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        int repairedGroups = 0;

        for (List<JobSourceListing> group : grouped.values()) {

            Set<String> externalIds = group.stream()
                    .map(JobSourceListing::getExternalJobId)
                    .collect(Collectors.toSet());

            if (externalIds.size() <= 1) {
                continue;
            }

            repairGroup(group);
            repairedGroups++;
        }

        log.info(
                "Greenhouse job repair completed: repairedGroups={}",
                repairedGroups
        );

        return repairedGroups;
    }

    private void repairGroup(List<JobSourceListing> listings) {

        listings.sort(
                Comparator.comparing(JobSourceListing::getId)
        );

        Job originalJob = listings.getFirst().getJob();

        updateJobIdentityFromListing(
                originalJob,
                listings.getFirst()
        );

        jobRepository.flush();

        for (int i = 1; i < listings.size(); i++) {

            JobSourceListing listing = listings.get(i);

            Job newJob =
                    createJobFromExistingData(
                            originalJob,
                            listing
                    );

            listing.reassignTo(newJob);

            jobSourceListingRepository.save(listing);

            rebuildLocations(
                    newJob,
                    listing
            );

            log.info(
                    "Repaired merged listing: listingId={}, externalJobId={}, oldJobId={}, newJobId={}",
                    listing.getId(),
                    listing.getExternalJobId(),
                    originalJob.getId(),
                    newJob.getId()
            );
        }
    }

    private void updateJobIdentityFromListing(
            Job job,
            JobSourceListing listing
    ) {
        RawJobListing rawJob =
                greenhouseJobMapper.map(
                        listing.getRawPayload()
                );

        List<NormalizedJobLocation> locations =
                normalizeLocations(rawJob);

        String normalizedApplicationUrl =
                jobUrlNormalizer.normalize(
                        rawJob.applicationUrl(),
                        "GREENHOUSE"
                );

        String correctedFingerprint =
                generateFingerprint(
                        job,
                        locations
                );

        job.updateNormalizedFields(
                job.getEmploymentType(),
                job.getSeniorityLevel(),
                job.getExperienceMinYears(),
                job.getExperienceMaxYears(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                normalizedApplicationUrl,
                correctedFingerprint
        );

        jobLocationService.replaceLocations(
                job.getId(),
                locations
        );
    }

    private Job createJobFromExistingData(
            Job originalJob,
            JobSourceListing listing
    ) {
        RawJobListing rawJob =
                greenhouseJobMapper.map(
                        listing.getRawPayload()
                );

        List<NormalizedJobLocation> locations =
                normalizeLocations(rawJob);

        String normalizedApplicationUrl =
                jobUrlNormalizer.normalize(
                        rawJob.applicationUrl(),
                        "GREENHOUSE"
                );

        String correctedFingerprint =
                generateFingerprint(
                        originalJob,
                        locations
                );

        Job newJob = new Job(
                originalJob.getCompany(),
                originalJob.getTitle(),
                originalJob.getNormalizedTitle(),
                originalJob.getDescription(),
                originalJob.getEmploymentType(),
                originalJob.getSeniorityLevel(),
                originalJob.getExperienceMinYears(),
                originalJob.getExperienceMaxYears(),
                originalJob.getSalaryMin(),
                originalJob.getSalaryMax(),
                originalJob.getSalaryCurrency(),
                listing.getSourcePostedAt(),
                originalJob.getExpiresAt(),
                normalizedApplicationUrl,
                correctedFingerprint,
                originalJob.getDescriptionHash()
        );

        return jobRepository.save(newJob);
    }

    private List<NormalizedJobLocation> normalizeLocations(
            RawJobListing rawJob
    ) {
        return jobTextNormalizer.normalizeJobLocations(
                rawJob.locations()
        );
    }

    private String generateFingerprint(
            Job job,
            List<NormalizedJobLocation> locations
    ) {
        String normalizedCompanyName =
                TextNormalizationSupport.normalizeWhitespaceAndCase(
                        job.getCompany().getDisplayName()
                );

        return jobFingerprintGenerator.generate(
                normalizedCompanyName,
                job.getNormalizedTitle(),
                locations,
                job.getEmploymentType(),
                job.getSeniorityLevel(),
                job.getExperienceMinYears(),
                job.getExperienceMaxYears()
        );
    }

    private void rebuildLocations(
            Job job,
            JobSourceListing listing
    ) {
        RawJobListing rawJob =
                greenhouseJobMapper.map(
                        listing.getRawPayload()
                );

        List<NormalizedJobLocation> locations =
                normalizeLocations(rawJob);

        jobLocationService.replaceLocations(
                job.getId(),
                locations
        );
    }
}