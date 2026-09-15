package com.samyak.job_intelligence.source.web.dto;


import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import tools.jackson.databind.JsonNode;

public record SourceConfigurationResponse(Long id, Long companyId, String sourceCode, boolean enabled, JsonNode configuration) {
    public static SourceConfigurationResponse from(SourceConfiguration sourceConfiguration){
        return new SourceConfigurationResponse(sourceConfiguration.getId(),
                sourceConfiguration.getCompany().getId(),
                sourceConfiguration.getJobSource().getCode(),
                sourceConfiguration.isEnabled(),
                sourceConfiguration.getConfiguration()
        );
    }
}
