package com.samyak.job_intelligence.llm;

import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig.Schema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIRequirementSchema {

    public Schema build() {


        Map<String, Object> requirementProperties = Map.of(

                "requirementType",
                Map.of(
                        "type", "string",
                        "enum", List.of(
                                "TECHNOLOGY",
                                "LANGUAGE",
                                "EDUCATION",
                                "EXPERIENCE",
                                "CERTIFICATION",
                                "OTHER"
                        )
                ),

                "values",
                Map.of(
                        "type", "array",
                        "items", Map.of("type", "string")
                ),

                "mandatory",
                Map.of("type", "boolean"),

                "yearsRequired",
                Map.of("type", List.of("number", "null")),

                "requirementText",
                Map.of("type", "string"),

                "requirementMatchMode",
                Map.of(
                        "type", "string",
                        "enum", List.of(
                                "SINGLE",
                                "ANY_OF",
                                "ALL_OF"
                        )
                )
        );

        Map<String, Object> requirementSchema = Map.of(
                "type", "object",
                "properties", requirementProperties,
                "required", List.of(
                        "requirementType",
                        "values",
                        "mandatory",
                        "yearsRequired",
                        "requirementText",
                        "requirementMatchMode"
                ),
                "additionalProperties", false
        );


        Map<String, Object> rootProperties = Map.of(
                "requirements",
                Map.of(
                        "type", "array",
                        "items", requirementSchema
                )
        );

        return Schema.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(rootProperties))
                .putAdditionalProperty(
                        "required",
                        JsonValue.from(List.of("requirements"))
                )
                .putAdditionalProperty(
                        "additionalProperties",
                        JsonValue.from(false)
                )
                .build();
    }
}