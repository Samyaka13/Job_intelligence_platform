package com.samyak.job_intelligence.llm;

import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GeminiRequirementSchema {

    public Schema build() {

        Schema requirementSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(
                                Map.of(
                                        "requirementType",
                                        Schema.builder()
                                                .type(Type.Known.STRING)
                                                .format("enum")
                                                .enum_(
                                                        "TECHNOLOGY",
                                                        "LANGUAGE",
                                                        "EDUCATION",
                                                        "EXPERIENCE",
                                                        "CERTIFICATION",
                                                        "OTHER"
                                                )
                                                .build(),

                                        "values",
                                        Schema.builder()
                                                .type(Type.Known.ARRAY)
                                                .items(
                                                        Schema.builder()
                                                                .type(Type.Known.STRING)
                                                                .build()
                                                )
                                                .build(),

                                        "mandatory",
                                        Schema.builder()
                                                .type(Type.Known.BOOLEAN)
                                                .build(),

                                        "yearsRequired",
                                        Schema.builder()
                                                .type(Type.Known.NUMBER)
                                                .nullable(true)
                                                .build(),

                                        "requirementText",
                                        Schema.builder()
                                                .type(Type.Known.STRING)
                                                .build(),

                                        "requirementMatchMode",
                                        Schema.builder()
                                                .type(Type.Known.STRING)
                                                .format("enum")
                                                .enum_(
                                                        "SINGLE",
                                                        "ANY_OF",
                                                        "ALL_OF"
                                                )
                                                .build()
                                )
                        )
                        .required(
                                "requirementType",
                                "values",
                                "mandatory",
                                "yearsRequired",
                                "requirementText",
                                "requirementMatchMode"
                        )
                        .build();

        return Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(
                        Map.of(
                                "requirements",
                                Schema.builder()
                                        .type(Type.Known.ARRAY)
                                        .items(requirementSchema)
                                        .build()
                        )
                )
                .required("requirements")
                .build();
    }
}