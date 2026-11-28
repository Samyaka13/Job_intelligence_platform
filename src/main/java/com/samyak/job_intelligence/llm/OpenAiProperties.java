package com.samyak.job_intelligence.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "llm.open-ai")
public record OpenAiProperties(
        String apiKey,
        String model
) {
}
