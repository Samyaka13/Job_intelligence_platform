package com.samyak.job_intelligence.source.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record SourceConfigurationCreateRequest(
        @NotBlank
        String sourceCode,
        @NotNull
        JsonNode configuration
) {
}
