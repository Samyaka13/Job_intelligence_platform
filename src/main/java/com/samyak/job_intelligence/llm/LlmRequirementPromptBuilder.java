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

                9. Return the requirement value as it appears naturally in the
                   source text. Normalization will be handled by the application.

                10. If no actual candidate requirements can be identified, return
                    an empty requirements array.
                
                11. Do not extract general job-level experience requirements when they are
                    already expressed as overall years of experience for the role. Overall
                    experience is handled separately by the application's deterministic
                    experience parser.
                
                12. Only populate requirementType=EXPERIENCE when the experience itself is
                    a distinct candidate requirement that needs semantic interpretation.
                
                13. Only populate yearsRequired when a number of years is explicitly tied
                    to the extracted requirement.
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
                      "value": "Java",
                      "mandatory": true,
                      "yearsRequired": null,
                      "requirementText": "Strong Java experience is required."
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