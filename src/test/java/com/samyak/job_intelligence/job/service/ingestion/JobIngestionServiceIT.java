package com.samyak.job_intelligence.job.service.ingestion;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.repository.CompanyRepository;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.domain.RemoteType;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.repository.JobRepository;
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
import static org.mockito.Mockito.when;

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

        when(collector.getSource()).thenReturn("GREENHOUSE");
        when(collector.collect()).thenReturn(List.of(rawListing));

        jobIngestionService.ingest(
                company.getId(),
                company.getCanonicalName(),
                collector
        );

        List<Job> jobs = jobRepository.findAll();

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
}