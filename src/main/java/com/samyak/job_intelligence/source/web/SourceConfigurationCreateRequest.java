package com.samyak.job_intelligence.source.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public class SourceConfigurationCreateRequest {
    @NotBlank
    String sourceCode;

    @NotNull
    JsonNode configuration;
}
