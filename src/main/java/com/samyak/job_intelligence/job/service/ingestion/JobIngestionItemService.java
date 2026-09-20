package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.service.JobLocationService;
import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobData;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementExtractionService;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementService;
import com.samyak.job_intelligence.llm.JobDescriptionCleaner;
import com.samyak.job_intelligence.source.service.JobSourceListingService;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class JobIngestionItemService {

    private final JobNormalizer jobNormalizer;
    private final JobService jobService;
    private final JobSourceListingService jobSourceListingService;
    private final JobLocationService jobLocationService;
    private final JobRequirementExtractionService jobRequirementExtractionService;
    private final JobRequirementService jobRequirementService;

    public JobIngestionItemService(
            JobNormalizer jobNormalizer,
            JobService jobService,
            JobSourceListingService jobSourceListingService,
            JobLocationService jobLocationService,
            JobRequirementExtractionService jobRequirementExtractionService,
            JobRequirementService jobRequirementService
    ) {
        this.jobNormalizer = jobNormalizer;
        this.jobService = jobService;
        this.jobSourceListingService = jobSourceListingService;
        this.jobLocationService = jobLocationService;
        this.jobRequirementExtractionService = jobRequirementExtractionService;
        this.jobRequirementService = jobRequirementService;
    }

    @Transactional
    public boolean ingestListing(
            Long companyId,
            String companyName,
            String sourceCode,
            RawJobListing rawJobListing
    ) {


        NormalizedJobData normalizedJobData =
                jobNormalizer.normalize(rawJobListing, companyName,sourceCode);

        Job job = jobSourceListingService.findJobBySourceAndExternalJobId(sourceCode, rawJobListing.externalJobId());

        if (job == null) {
            job = jobService.findByCanonicalFingerPrint(
                    normalizedJobData.canonicalFingerprint()
            );
        }

        System.out.println(
                "LOOKUP -> fingerprint=" +
                        normalizedJobData.canonicalFingerprint() +
                        ", jobId=" +
                        (job == null ? null : job.getId())
        );

        boolean created = false;
        boolean descriptionChanged = false;

        if (job == null) {
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

        } else {
            descriptionChanged = !Objects.equals(job.getDescriptionHash(),normalizedJobData.descriptionHash());
            job.markSeen(Instant.now());

            System.out.println(
                    "JOB UPDATE -> id=" + job.getId()
                            + ", min=" + normalizedJobData.experienceMinYears()
                            + ", max=" + normalizedJobData.experienceMaxYears()
            );
            if(job.getId() == 91){
                System.out.println("ID WITH 91 got updated");
            }
            job.updateNormalizedFields(
                    normalizedJobData.employmentType(),
                    normalizedJobData.seniorityLevel(),
                    normalizedJobData.experienceMinYears(),
                    normalizedJobData.experienceMaxYears(),
                    normalizedJobData.salaryMin(),
                    normalizedJobData.salaryMax(),
                    normalizedJobData.salaryCurrency(),
                    normalizedJobData.normalisedApplicationUrl(),
                    normalizedJobData.canonicalFingerprint()
            );

            if(descriptionChanged){
                job.updateDescription(
                        normalizedJobData.description(),
                        normalizedJobData.descriptionHash()
                );
            }
        }

        jobSourceListingService.createOrRefresh(
                job.getId(),
                sourceCode,
                rawJobListing.externalJobId(),
                normalizedJobData.normalizedSourceUrl(),
                rawJobListing.rawPayload(),
                rawJobListing.postedAt()
        );

        jobLocationService.replaceLocations(
                job.getId(),
                normalizedJobData.locations()
        );

        if(created ||  descriptionChanged) {

           try(MDC.MDCCloseable ignored =
                       MDC.putCloseable("jobId", String.valueOf(job.getId()))){
               System.out.printf(
                       "LLM INPUT | job=%d | descriptionChars=%d%n",
                       job.getId(),
                       JobDescriptionCleaner.clean(normalizedJobData.description()).length()
               );
                List<ExtractedJobRequirement> requirements =
                        jobRequirementExtractionService.extract(
                                JobDescriptionCleaner.clean(normalizedJobData.description())
                        );

            jobRequirementService.replaceRequirements(
                    job.getId(),
                    requirements
            );
           }
        }

        return created;
    }
}