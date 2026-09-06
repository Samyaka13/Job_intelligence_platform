package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import com.samyak.job_intelligence.job.service.parsing.EmploymentTypeParser;
import com.samyak.job_intelligence.job.service.parsing.ExperienceParser;
import com.samyak.job_intelligence.job.service.parsing.SalaryParser;
import com.samyak.job_intelligence.job.service.parsing.SeniorityParser;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobNormalizerTest {

    private final JobNormalizer normalizer = new JobNormalizer(
            new JobTextNormalizer(),
            new JobUrlNormalizer(),
            new DescriptionHashGenerator(),
            new ExperienceParser(),
            new EmploymentTypeParser(),
            new SeniorityParser(),
            new SalaryParser(),
            new JobFingerprintGenerator()
    );

    @Test
    void shouldNormalizeAndParseJob() {
        RawJobListing rawJobListing = new RawJobListing(
                "123",
                "  Senior  Software   Engineer  ",
                """
                        We are looking for a backend engineer.
                        
                        Candidates should have 2-4 years of experience.
                        Salary: 8 LPA - 12 LPA INR
                        """,
                " HTTPS://example.com/jobs/123?source=linkedin ",
                "https://example.com/apply/123?utm_source=linkedin",
                List.of(),
                Instant.parse("2026-09-02T10:00:00Z"),
                null
        );

        NormalizedJobData result =
                normalizer.normalize(rawJobListing, "  Example   Company  ");

        assertEquals("  Example   Company  ", result.companyName());
        assertEquals("example company", result.normalizedCompanyName());

        assertEquals(
                "  Senior  Software   Engineer  ",
                result.title()
        );
        assertEquals(
                "senior software engineer",
                result.normalizedTitle()
        );

        assertEquals(
                "We are looking for a backend engineer.\n\nCandidates should have 2-4 years of experience.\nSalary: 8 LPA - 12 LPA INR",
                result.description()
        );

        assertEquals(
                "https://example.com/jobs/123",
                result.normalizedSourceUrl()
        );

        assertEquals(
                "https://example.com/apply/123",
                result.normalisedApplicationUrl()
        );

        assertEquals(
                EmploymentType.UNKNOWN,
                result.employmentType()
        );

        assertEquals(
                SeniorityLevel.SENIOR,
                result.seniorityLevel()
        );

        assertEquals(
                0,
                result.experienceMinYears().compareTo(
                        new BigDecimal("2")
                )
        );

        assertEquals(
                0,
                result.experienceMaxYears().compareTo(
                        new BigDecimal("4")
                )
        );

        assertEquals(
                new BigDecimal("800000"),
                result.salaryMin()
        );

        assertEquals(
                new BigDecimal("1200000"),
                result.salaryMax()
        );

        assertEquals(
                "INR",
                result.salaryCurrency()
        );

        assertEquals(
                Instant.parse("2026-09-02T10:00:00Z"),
                result.postedAt()
        );

        assertNotNull(result.descriptionHash());
        assertEquals(64, result.descriptionHash().length());
    }
}