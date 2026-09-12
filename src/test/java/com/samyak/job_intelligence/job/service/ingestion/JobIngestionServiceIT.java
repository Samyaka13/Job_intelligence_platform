package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.*;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.repository.JobRepository;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementExtractionService;
import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceListing;
import com.samyak.job_intelligence.source.repository.JobSourceListingRepository;
import com.samyak.job_intelligence.source.repository.JobSourceRepository;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.RawJobListing;
import com.samyak.job_intelligence.job.service.normalization.JobNormalizer;
import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
class JobIngestionServiceIT {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("job_intelligence_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JobIngestionService jobIngestionService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    @Autowired
    private JobSourceListingRepository jobSourceListingRepository;

    @MockitoBean
    private JobSourceCollector collector;

    @Autowired
    private JobNormalizer jobNormalizer;

    @Autowired
    private JobLocationRepository jobLocationRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @MockitoBean
    private JobRequirementExtractionService jobRequirementExtractionService;


    @Test
    @Transactional
    void shouldPersistNewJobAndSourceListing() {

        Company company = companyRepository.save(
                new Company(
                        "google",
                        "Google",
                        "https://www.google.com"
                )
        );

        JobSource source = jobSourceRepository.findByCode("GREENHOUSE").orElseThrow();

        RawJobListing rawListing =
                new RawJobListing(
                        "123",
                        "Backend Engineer",
                        "Build backend services using Java and Spring Boot.",
                        "https://example.com/job/123",
                        "https://example.com/apply/123",List.of(
                        new RawJobLocation(
                                null,
                                null,
                                null,
                                "Bangalore, Karnataka, India"
                        )
                ),
                        Instant.now(),
                        null
                );
        List<ExtractedJobRequirement> extractedRequirements =
                List.of(
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Java",
                                "java",
                                true,
                                null,
                                "Strong Java experience is required."
                        ),
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Kafka",
                                "kafka",
                                false,
                                null,
                                "Experience with Kafka is a plus."
                        )
                );
        when(collector.getSource()).thenReturn("GREENHOUSE");
        when(collector.collect()).thenReturn(List.of(rawListing));
        when(jobRequirementExtractionService.extract(
                eq("Build backend services using Java and Spring Boot.")
        )).thenReturn(extractedRequirements);
        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        verify(jobRequirementExtractionService)
                .extract(
                        eq("Build backend services using Java and Spring Boot.")
                );



        List<Job> jobs = jobRepository.findAll();
        for (Job j : jobs) {
            System.out.println("Job ID = " + j.getId());
        }

        assertThat(jobs).hasSize(1);

        Job job = jobs.getFirst();

        assertThat(job.getId()).isNotNull();
        assertThat(job.getCompany().getId())
                .isEqualTo(company.getId());

        assertThat(job.getTitle())
                .isEqualTo("Backend Engineer");

        assertThat(job.getCanonicalFingerprint())
                .isNotBlank();

        List<JobSourceListing> listings =
                jobSourceListingRepository.findAll();

        assertThat(listings).hasSize(1);

        JobSourceListing listing = listings.getFirst();

        assertThat(listing.getJob().getId())
                .isEqualTo(job.getId());

        assertThat(listing.getJobSource().getId())
                .isEqualTo(source.getId());

        assertThat(listing.getExternalJobId())
                .isEqualTo("123");

        List<JobLocation> locations =
                jobLocationRepository.findByJobId(job.getId());

        assertThat(locations)
                .hasSize(1);

        JobLocation location = locations.getFirst();

        assertThat(location.getJob().getId())
                .isEqualTo(job.getId());

        assertThat(location.getDisplayText())
                .isEqualTo("bangalore, karnataka, india");

        assertThat(location.getRemoteType())
                .isEqualTo(RemoteType.UNKNOWN);

        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(job.getId());

        assertThat(requirements)
                .hasSize(2);

        assertThat(requirements)
                .extracting(JobRequirement::getNormalizedValue)
                .containsExactlyInAnyOrder(
                        "java",
                        "kafka"
                );

        JobRequirement javaRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.getNormalizedValue()
                                        .equals("java")
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(javaRequirement.isMandatory())
                .isTrue();

        JobRequirement kafkaRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.getNormalizedValue()
                                        .equals("kafka")
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(kafkaRequirement.isMandatory())
                .isFalse();

    }

    @Test
    @Transactional
    void shouldReuseExistingJobWhenSameFingerprintIsIngestedAgain() {

        Company company = companyRepository.save(
                new Company(
                        "amazon",
                        "Amazon",
                        "https://www.amazon.com"
                )
        );

        JobSource source = jobSourceRepository.findByCode("GREENHOUSE").orElseThrow();

        RawJobListing rawListing =
                new RawJobListing(
                        "456",
                        "Backend Engineer",
                        "Build backend services using Java and Spring Boot.",
                        "https://example.com/job/456",
                        "https://example.com/apply/456",
                        List.of(
                                new RawJobLocation(
                                        null,
                                        null,
                                        null,
                                        "Bangalore, Karnataka, India"
                                )
                        ),
                        Instant.now(),
                        null
                );

        when(collector.getSource()).thenReturn("GREENHOUSE");
        when(collector.collect()).thenReturn(List.of(rawListing));

        // First ingestion
        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        List<Job> jobsAfterFirstIngestion = jobRepository.findAll();

        assertThat(jobsAfterFirstIngestion)
                .hasSize(1);

        Job firstJob = jobsAfterFirstIngestion.getFirst();

        Long firstJobId = firstJob.getId();

        // Second ingestion of the same listing
        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        List<Job> jobsAfterSecondIngestion = jobRepository.findAll();

        assertThat(jobsAfterSecondIngestion)
                .hasSize(1);

        Job secondJob = jobsAfterSecondIngestion.getFirst();

        assertThat(secondJob.getId())
                .isEqualTo(firstJobId);

        List<JobSourceListing> listings =
                jobSourceListingRepository.findAll();

        assertThat(listings)
                .hasSize(1);

        JobSourceListing listing = listings.getFirst();

        assertThat(listing.getJob().getId())
                .isEqualTo(firstJobId);

        assertThat(listing.getExternalJobId())
                .isEqualTo("456");

        assertThat(listing.isActive())
                .isTrue();
    }

    @Test
    @Transactional
    void shouldUpdateExistingJobAndReplaceRequirementsOnReingestion() {

        Company company = companyRepository.save(
                new Company(
                        "google",
                        "Google",
                        "https://www.google.com"
                )
        );

        JobSource source =
                jobSourceRepository.findByCode("GREENHOUSE")
                        .orElseThrow();

        RawJobListing rawListing =
                new RawJobListing(
                        "123",
                        "Backend Engineer",
                        "Build backend services using Java and Spring Boot.",
                        "https://example.com/job/123",
                        "https://example.com/apply/123",
                        List.of(
                                new RawJobLocation(
                                        null,
                                        null,
                                        null,
                                        "Bangalore, Karnataka, India"
                                )
                        ),
                        Instant.now(),
                        null
                );

        // First ingestion requirements: Java + Kafka
        List<ExtractedJobRequirement> firstRequirements =
                List.of(
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Java",
                                "java",
                                true,
                                null,
                                "Strong Java experience is required."
                        ),
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Kafka",
                                "kafka",
                                false,
                                null,
                                "Experience with Kafka is a plus."
                        )
                );

        // Second ingestion requirements: Java + Docker
        List<ExtractedJobRequirement> secondRequirements =
                List.of(
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Java",
                                "java",
                                true,
                                null,
                                "Strong Java experience is required."
                        ),
                        new ExtractedJobRequirement(
                                RequirementType.TECHNOLOGY,
                                "Docker",
                                "docker",
                                false,
                                null,
                                "Experience with Docker is a plus."
                        )
                );

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(collector.collect())
                .thenReturn(List.of(rawListing));

        when(jobRequirementExtractionService.extract(
                eq("Build backend services using Java and Spring Boot.")
        )).thenReturn(
                firstRequirements,
                secondRequirements
        );

        // First ingestion
        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        Long originalJobId =
                jobRepository.findAll()
                        .getFirst()
                        .getId();

        // Second ingestion of the same listing
        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        // Only one canonical job should exist
        List<Job> jobs = jobRepository.findAll();

        assertThat(jobs)
                .hasSize(1);

        assertThat(jobs.getFirst().getId())
                .isEqualTo(originalJobId);

        // Only one source listing should exist
        assertThat(jobSourceListingRepository.findAll())
                .hasSize(1);

        JobSourceListing listing =
                jobSourceListingRepository.findAll()
                        .getFirst();

        assertThat(listing.getJob().getId())
                .isEqualTo(originalJobId);

        assertThat(listing.getExternalJobId())
                .isEqualTo("123");

        assertThat(listing.getJobSource().getId())
                .isEqualTo(source.getId());

        // Requirements should have been replaced
        List<JobRequirement> requirements =
                jobRequirementRepository.findByJobId(originalJobId);

        assertThat(requirements)
                .hasSize(2);

        assertThat(requirements)
                .extracting(JobRequirement::getNormalizedValue)
                .containsExactlyInAnyOrder(
                        "java",
                        "docker"
                );

        assertThat(requirements)
                .extracting(JobRequirement::getNormalizedValue)
                .doesNotContain("kafka");

        JobRequirement javaRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.getNormalizedValue().equals("java")
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(javaRequirement.isMandatory())
                .isTrue();

        JobRequirement dockerRequirement =
                requirements.stream()
                        .filter(requirement ->
                                requirement.getNormalizedValue().equals("docker")
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(dockerRequirement.isMandatory())
                .isFalse();

        verify(jobRequirementExtractionService, times(2))
                .extract(
                        eq("Build backend services using Java and Spring Boot.")
                );
    }


    @Test
    @Transactional
    void shouldMarkMissingSourceListingInactiveWhenItDisappearsFromSource() {

        Company company = companyRepository.save(
                new Company(
                        "google",
                        "Google",
                        "https://www.google.com"
                )
        );

        JobSource source =
                jobSourceRepository.findByCode("GREENHOUSE")
                        .orElseThrow();

        RawJobListing jobA =
                new RawJobListing(
                        "100",
                        "Backend Engineer",
                        "Build backend services.",
                        "https://example.com/job/100",
                        "https://example.com/apply/100",
                        List.of(
                                new RawJobLocation(
                                        null,
                                        null,
                                        null,
                                        "Bangalore, Karnataka, India"
                                )
                        ),
                        Instant.now(),
                        null
                );

        RawJobListing jobB =
                new RawJobListing(
                        "200",
                        "Frontend Engineer",
                        "Build frontend applications.",
                        "https://example.com/job/200",
                        "https://example.com/apply/200",
                        List.of(
                                new RawJobLocation(
                                        null,
                                        null,
                                        null,
                                        "Bangalore, Karnataka, India"
                                )
                        ),
                        Instant.now(),
                        null
                );

        when(collector.getSource())
                .thenReturn("GREENHOUSE");

        when(jobRequirementExtractionService.extract(anyString()))
                .thenReturn(List.of());

        // First collection: Job A + Job B
        when(collector.collect())
                .thenReturn(List.of(jobA, jobB));

        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        List<JobSourceListing> listingsAfterFirstIngestion =
                jobSourceListingRepository.findAll();

        assertThat(listingsAfterFirstIngestion)
                .hasSize(2);

        // Second collection: Job A only
        when(collector.collect())
                .thenReturn(List.of(jobA));

        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        List<JobSourceListing> listingsAfterSecondIngestion =
                jobSourceListingRepository.findAll();

        assertThat(listingsAfterSecondIngestion)
                .hasSize(2);

        JobSourceListing jobAListing =
                listingsAfterSecondIngestion.stream()
                        .filter(listing ->
                                listing.getExternalJobId().equals("100")
                        )
                        .findFirst()
                        .orElseThrow();

        JobSourceListing jobBListing =
                listingsAfterSecondIngestion.stream()
                        .filter(listing ->
                                listing.getExternalJobId().equals("200")
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(jobAListing.isActive())
                .isTrue();

        assertThat(jobBListing.isActive())
                .isFalse();

        assertThat(jobAListing.getJobSource().getId())
                .isEqualTo(source.getId());

        assertThat(jobBListing.getJobSource().getId())
                .isEqualTo(source.getId());
    }
}