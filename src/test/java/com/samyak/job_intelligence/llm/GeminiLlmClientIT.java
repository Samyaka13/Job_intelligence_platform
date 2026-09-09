package com.samyak.job_intelligence.llm;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
@SpringBootTest
class GeminiLlmClientIT {

    @Autowired
    private GeminiLlmClient geminiLlmClient;

    @Test
    void shouldGenerateStructuredRequirementJson() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        Assumptions.assumeTrue(
                apiKey != null && !apiKey.isBlank(),
                "GEMINI_API_KEY is not configured"
        );

        String systemPrompt = """
                Extract candidate requirements from the job description.

                Return only requirements that candidates are expected
                or preferred to satisfy.

                Classify mandatory as follows:
                - true: required, must have, mandatory, essential
                - false: preferred, optional, plus, bonus, nice to have

                Do not treat technologies merely mentioned in a company's
                description as candidate requirements.

                Return only the requested JSON structure.
                """;

        String userPrompt = """
                Extract requirements from this job description:

                We are looking for a backend engineer.

                Strong Java and Spring Boot experience is required.
                Experience with Kafka is a plus.
                Familiarity with AWS would be beneficial.

                2-4 years of backend development experience is required.
                """;

        String response =
                geminiLlmClient.generate(
                        systemPrompt,
                        userPrompt
                );

        assertThat(response)
                .isNotBlank();

        System.out.println(response);

        assertThat(response)
                .contains("requirements");
    }
}