package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobFingerprintGeneratorTest {

    private final JobFingerprintGenerator generator =
            new JobFingerprintGenerator();

    @Test
    void shouldGenerateSameFingerprintForSameInput() {

        String first = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "bengaluru",
                                "karnataka",
                                "india",
                                "Bengaluru, Karnataka, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        String second = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "bengaluru",
                                "karnataka",
                                "india",
                                "Bengaluru, Karnataka, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldGenerateDifferentFingerprintWhenIdentityChanges() {

        String first = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "bengaluru",
                                "karnataka",
                                "india",
                                "Bengaluru, Karnataka, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        String second = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "hyderabad",
                                "telangana",
                                "india",
                                "Hyderabad, Telangana, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void shouldGenerateSha256Fingerprint() {

        String fingerprint = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "bengaluru",
                                "karnataka",
                                "india",
                                "Bengaluru, Karnataka, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        assertThat(fingerprint)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void shouldHandleNullValues() {

        String fingerprint = generator.generate(
                null,
                "backend engineer",
                null,
                null,
                null,
                null,
                null
        );

        assertThat(fingerprint)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void shouldGenerateSameFingerprintRegardlessOfLocationOrder() {

        String first = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "bengaluru",
                                "karnataka",
                                "india",
                                "Bengaluru, Karnataka, India"
                        ),
                        new NormalizedJobLocation(
                                "hyderabad",
                                "telangana",
                                "india",
                                "Hyderabad, Telangana, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        String second = generator.generate(
                "google",
                "backend engineer",
                List.of(
                        new NormalizedJobLocation(
                                "hyderabad",
                                "telangana",
                                "india",
                                "Hyderabad, Telangana, India"
                        ),
                        new NormalizedJobLocation(
                                "bengaluru",
                                "karnataka",
                                "india",
                                "Bengaluru, Karnataka, India"
                        )
                ),
                EmploymentType.FULL_TIME,
                SeniorityLevel.ENTRY,
                new BigDecimal("0"),
                new BigDecimal("2")
        );

        assertThat(first)
                .isEqualTo(second);
    }
}