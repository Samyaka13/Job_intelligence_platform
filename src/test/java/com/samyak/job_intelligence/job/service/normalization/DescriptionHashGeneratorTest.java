package com.samyak.job_intelligence.job.service.normalization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DescriptionHashGeneratorTest {

    private final JobDescriptionNormalizer descriptionNormalizer =
            new JobDescriptionNormalizer();

    private final DescriptionHashGenerator hashGenerator =
            new DescriptionHashGenerator();

    @Test
    void shouldGenerateSameHashForDescriptionsWithDifferentFormatting() {

        String firstDescription = """
                Build REST APIs.


                Work with Java and Spring Boot.
                """;

        String secondDescription = """
                Build REST APIs.
                
                Work with Java and Spring Boot.
                """;

        String firstNormalized =
                descriptionNormalizer.normalize(firstDescription);

        String secondNormalized =
                descriptionNormalizer.normalize(secondDescription);

        String firstHash =
                hashGenerator.generate(firstNormalized);

        String secondHash =
                hashGenerator.generate(secondNormalized);

        assertThat(firstNormalized)
                .isEqualTo(secondNormalized);

        assertThat(firstHash)
                .isEqualTo(secondHash);
    }

    @Test
    void shouldGenerateDifferentHashWhenDescriptionContentChanges() {

        String firstDescription =
                "Build REST APIs using Java and Spring Boot.";

        String secondDescription =
                "Build REST APIs using Java, Spring Boot and PostgreSQL.";

        String firstHash =
                hashGenerator.generate(
                        descriptionNormalizer.normalize(firstDescription)
                );

        String secondHash =
                hashGenerator.generate(
                        descriptionNormalizer.normalize(secondDescription)
                );

        assertThat(firstHash)
                .isNotEqualTo(secondHash);
    }

    @Test
    void shouldGenerate64CharacterSha256Hash() {

        String description =
                "Build scalable backend services.";

        String normalized =
                descriptionNormalizer.normalize(description);

        String hash =
                hashGenerator.generate(normalized);

        assertThat(hash)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void shouldReturnNullForNullDescription() {

        String normalized =
                descriptionNormalizer.normalize(null);

        String hash =
                hashGenerator.generate(normalized);

        assertThat(hash).isNull();
    }
}