package com.samyak.job_intelligence.llm;

import org.springframework.stereotype.Component;

@Component
public class LlmRequirementPromptBuilder {

    public String systemPrompt() {
        return """
                You extract candidate requirements from job descriptions.

                Your task is to identify requirements that a candidate is expected
                or preferred to satisfy.

                Return ONLY valid JSON matching the requested schema.

                Requirement types allowed:
                - TECHNOLOGY
                - LANGUAGE
                - EDUCATION
                - EXPERIENCE
                - CERTIFICATION
                - OTHER

                Match modes allowed:
                - SINGLE
                - ANY_OF
                - ALL_OF

                Rules:

                1. Extract actual candidate requirements, not technologies or concepts
                   merely mentioned in the company's description.

                2. mandatory=true means the candidate is expected to satisfy the
                   requirement for the role.

                3. mandatory=false means the requirement is preferred, optional,
                   a plus, a bonus, desirable, or otherwise not strictly required.

                4. Use the surrounding context to determine whether a requirement
                   is mandatory or non-mandatory.

                5. Do not invent requirements that are not supported by the text.

                6. yearsRequired should only be populated when the number of years
                   is explicitly tied to that specific requirement.

                7. Do not put a general job experience requirement into
                   yearsRequired for a technology unless the text explicitly ties
                   the experience to that technology.

                8. requirementText must contain the smallest useful piece of source
                   text that supports the extracted requirement.

                9. Extract atomic candidate requirements into the values array.
                   Do not put an entire sentence, paragraph, list item, or
                   multi-action phrase into a single value.

                10. Use matchMode=SINGLE when the requirement contains one
                    independent value.

                11. Use matchMode=ANY_OF when satisfying any one of the values
                    is sufficient.

                    Example:
                    "Java, Python, Go, or Ruby"

                    values:
                    ["Java", "Python", "Go", "Ruby"]

                    matchMode:
                    "ANY_OF"

                12. Use matchMode=ALL_OF when all listed values are required.

                    Example:
                    "Java and Spring Boot"

                    values:
                    ["Java", "Spring Boot"]

                    matchMode:
                    "ALL_OF"

                13. Do not combine alternatives into a single value.

                    WRONG:
                    "Java, Python, or Go"

                    CORRECT:
                    ["Java", "Python", "Go"]

                14. Do not extract general job-level experience requirements when
                    they are already expressed as overall years of experience for
                    the role. Overall experience is handled separately by the
                    application's deterministic experience parser.

                15. Only populate requirementType=EXPERIENCE when the experience
                    itself is a distinct candidate requirement that needs semantic
                    interpretation.

                16. Only populate yearsRequired when a number of years is explicitly
                    tied to the extracted requirement.

                17. Return the requirement value as it appears naturally in the
                    source text. Normalization will be handled by the application.

                18. If no actual candidate requirements can be identified, return
                    an empty requirements array.
                """;
    }

    public String userPrompt(String jobDescription) {
        return """
                Extract the candidate requirements from the following job description.

                Return JSON in exactly this shape:

                {
                  "requirements": [
                    {
                      "requirementType": "TECHNOLOGY",
                      "values": ["Java"],
                      "mandatory": true,
                      "yearsRequired": null,
                      "requirementText": "Strong Java experience is required.",
                      "matchMode": "SINGLE"
                    }
                  ]
                }

                Job description:
                ---
                %s
                ---
                """.formatted(jobDescription);
    }
}