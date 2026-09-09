package com.samyak.job_intelligence.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.gemini")
public record GeminiProperties(String apiKey,String model) {
}
