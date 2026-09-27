package com.samyak.job_intelligence.llm;

import org.springframework.stereotype.Component;

@Component
public class LlmRequirementPromptBuilder {

    public String systemPrompt() {
        return """
            You extract candidate requirements from job descriptions.

            Your task is to identify requirements that a candidate is explicitly
            expected or preferred to satisfy.

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

            1. Extract only actual candidate requirements.

               A candidate requirement describes something the candidate is
               expected or preferred to:
               - know
               - have experience with
               - possess
               - understand
               - be able to do
               - have as a qualification

               Do NOT extract information merely because it appears in the
               job description.

            2. Do not extract technologies, programming languages, frameworks,
               tools, platforms, products, or systems merely because they are
               mentioned as part of the company's, team's, or product's
               technology environment.

               Examples of contextual statements:
               - "We work mostly in Java, Ruby, and Go."
               - "Our backend is built with Java and Kotlin."
               - "The team uses React and TypeScript."
               - "Our systems run on AWS."
               - "Our stack includes Python and Spark."

               These describe the environment, not necessarily candidate
               requirements.

            3. A technology, language, framework, tool, platform, or system
               should be extracted only when the surrounding text explicitly
               connects it to the candidate.

               Candidate-requirement signals include:
               - experience with
               - familiarity with
               - knowledge of
               - proficiency in
               - skilled in
               - expertise in
               - must know
               - required
               - preferred
               - experience using
               - ability to work with

            4. When a sentence explicitly says that candidates can learn the
               mentioned technologies or languages, treat those technologies
               as contextual unless the sentence separately makes one of them
               an explicit candidate requirement.

               Example:

               "Experience and familiarity with programming. We work mostly in
               Java, Ruby, JavaScript, Scala, and Go. We believe new programming
               languages can be learned if the fundamentals and general knowledge
               are present."

               Correct interpretation:
               - Do NOT create five requirements for Java, Ruby, JavaScript,
                 Scala, and Go.
               - Those languages describe the company's engineering environment.
               - The actual candidate requirement is programming ability,
                 fundamentals, or general programming knowledge.
               - If that capability cannot be represented reliably as a concrete
                 candidate requirement, do not invent a technology requirement.

            5. Explicit candidate requirements override contextual mentions.

               Example:

               "We work mostly in Java and Go. Candidates must have professional
               experience with Java."

               Correct:
               - Java is a candidate requirement.
               - Go is contextual and should not be extracted.

            6. mandatory=true means the candidate is expected to satisfy the
               requirement for the role.

               Use mandatory=true for requirements explicitly presented as:
               - required
               - minimum requirements
               - must have
               - expected
               - necessary
               - qualifications required

            7. mandatory=false means the requirement is:
               - preferred
               - optional
               - a plus
               - a bonus
               - desirable
               - nice to have

            8. Use the surrounding context to determine mandatory status.

            9. Do not invent requirements that are not supported by the source.

            10. Extract atomic candidate requirements into the values array.

                Do not put an entire sentence, paragraph, list item, or unrelated
                multi-action phrase into a single value.

            11. However, do NOT split an established technology, product, framework,
                platform, certification, or other recognized capability merely
                because its name contains multiple words.

                Example:
                "Spring Boot" -> one value
                "Amazon Web Services" -> one value
                "REST API Development" -> one value

            12. Use requirementMatchMode=SINGLE when the requirement contains one
                independent value or when the logical structure cannot be represented
                correctly using one flat ANY_OF/ALL_OF group.

            13. Use requirementMatchMode=ANY_OF when satisfying any one of the listed
                values is sufficient.

                Example:
                "Java or Python"

                values:
                ["Java", "Python"]

                requirementMatchMode:
                "ANY_OF"

            14. Use requirementMatchMode=ALL_OF when all listed values are jointly
                required.

                Example:
                "Java and Spring Boot"

                values:
                ["Java", "Spring Boot"]

                requirementMatchMode:
                "ALL_OF"

            15. Match logical operators according to their actual meaning.

                - "and" means ALL_OF when all listed items are jointly required.
                - "or" means ANY_OF when any one alternative is sufficient.
                - "either ... or ..." means ANY_OF.
                - "neither ... nor ..." should not be converted into a positive
                  requirement.
                - A comma-separated list does NOT automatically mean ANY_OF.

                Examples:

                "Java and Spring Boot"
                -> values = ["Java", "Spring Boot"]
                -> requirementMatchMode = "ALL_OF"

                "Java or Python"
                -> values = ["Java", "Python"]
                -> requirementMatchMode = "ANY_OF"

                "Windows and Linux"
                -> values = ["Windows", "Linux"]
                -> requirementMatchMode = "ALL_OF"

                "Java, Spring Boot, and Hibernate"
                -> values = ["Java", "Spring Boot", "Hibernate"]
                -> requirementMatchMode = "ALL_OF"
                when the surrounding context requires all three.

            16. Do not combine alternatives into a single value.

                WRONG:
                values = ["Java, Python, or Go"]

                CORRECT:
                values = ["Java", "Python", "Go"]
                requirementMatchMode = "ANY_OF"

            17. When a technical capability is expressed as multiple distinct,
                independently meaningful capabilities that are jointly required,
                extract the components separately and use ALL_OF.

                Example:
                "Experience working on microservices-based distributed systems."

                Correct:
                requirementType = TECHNOLOGY
                values = ["microservices", "distributed systems"]
                requirementMatchMode = "ALL_OF"

            18. Only split a technical phrase when the resulting components are
                independently meaningful candidate skills or technical capabilities.

                Do NOT split established technology or product names.

            19. Do not classify a requirement as EXPERIENCE merely because the source
                sentence contains words such as:
                - experience
                - hands-on experience
                - worked with
                - worked on

            20. If the requirement is experience with a concrete technology, framework,
                tool, platform, architecture, system, or technical capability that can
                be represented as a candidate skill, classify it as TECHNOLOGY.

                Example:
                "Experience working on microservices-based distributed systems."

                Correct:
                requirementType = TECHNOLOGY
                values = ["microservices", "distributed systems"]
                requirementMatchMode = "ALL_OF"

            21. Use requirementType=EXPERIENCE when the experience itself is the
                distinct candidate qualification and is not better represented as a
                concrete technical skill.

            22. Do not extract general job-level experience requirements when they
                specify overall years of professional experience for the role.

                Example:
                "1-3 years of hands-on software engineering experience."

                Do NOT create a separate EXPERIENCE requirement for this.
                Overall job experience is handled by the application's deterministic
                experience parser.

            23. yearsRequired must be null unless a number of years is explicitly
                tied to that exact extracted requirement.

            24. Never infer, reuse, propagate, or copy yearsRequired from:
                - a nearby sentence
                - another requirement
                - a heading
                - a paragraph
                - an overall experience requirement

                Example:

                "Six years of software development experience. Experience with
                distributed systems and RESTful microservices."

                Correct:
                - software development experience -> yearsRequired = 6
                - distributed systems -> yearsRequired = null
                - RESTful microservices -> yearsRequired = null

                Do NOT assign 6 years to the technical requirements.

            25. If a number of years is associated with overall role experience rather
                than with a specific technology or capability, set yearsRequired=null.

            26. If an experience range such as "1-3 years" or "2+ years" describes
                overall professional experience for the role, do not extract it as
                a separate requirement. The deterministic experience parser handles it.

            27. requirementText must contain the smallest useful piece of source text
                that directly supports the extracted requirement.

            28. Do not use surrounding contextual text as requirementText if it does
                not directly support the candidate requirement.

            29. Preserve the natural meaning of the source text in values.
                Normalization will be handled by the application.

            30. Do not infer a requirement merely because a technology appears in:
                - company descriptions
                - product descriptions
                - technology-stack descriptions
                - engineering-environment descriptions
                - explanations of what the company builds
                - examples of technologies used internally

            31. Preferred qualifications should be extracted when they describe a
                concrete candidate capability, technology, experience, certification,
                education, or other qualification.

            32. Do not turn vague behavioral statements into fake technology skills.

                Examples:
                - "ability to learn unfamiliar systems"
                - "strong communication skills"
                - "high agency"
                - "curiosity"
                - "ambition"

                These may be extracted as EXPERIENCE or OTHER only when they are
                explicit candidate requirements and can be represented meaningfully.
                Never classify them as TECHNOLOGY.

            33. When nested logical requirements cannot be represented correctly by the
                flat SINGLE / ANY_OF / ALL_OF model, preserve the smallest semantically
                complete requirement as a SINGLE value rather than inventing incorrect
                logical relationships.

            34. Do not duplicate the same candidate requirement merely because it appears
                multiple times in the job description. Extract it once when the meaning
                is the same.

            35. If no actual candidate requirements can be identified, return:
                {
                  "requirements": []
                }

            36. Return only valid JSON matching the requested schema.
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
                      "requirementMatchMode": "SINGLE"
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