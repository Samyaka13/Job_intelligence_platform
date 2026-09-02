package com.samyak.job_intelligence.job.service.normalization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobFingerprintGeneratorTest {

    private final JobFingerprintGenerator generator =
            new JobFingerprintGenerator();

    @Test
    void shouldGenerateSameFingerprintForSameInput() {

        String first = generator.generate(
                "google",
                "backend engineer",
                "bengaluru",
                "FULL_TIME",
                "ENTRY",
                "0-2"
        );

        String second = generator.generate(
                "google",
                "backend engineer",
                "bengaluru",
                "FULL_TIME",
                "ENTRY",
                "0-2"
        );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldGenerateDifferentFingerprintWhenIdentityChanges() {

        String first = generator.generate(
                "google",
                "backend engineer",
                "bengaluru",
                "FULL_TIME",
                "ENTRY",
                "0-2"
        );

        String second = generator.generate(
                "google",
                "backend engineer",
                "hyderabad",
                "FULL_TIME",
                "ENTRY",
                "0-2"
        );

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void shouldGenerateSha256Fingerprint() {

        String fingerprint = generator.generate(
                "google",
                "backend engineer",
                "bengaluru",
                "FULL_TIME",
                "ENTRY",
                "0-2"
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
                "FULL_TIME",
                "ENTRY",
                null
        );

        assertThat(fingerprint)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }
}