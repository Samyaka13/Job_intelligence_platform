package com.samyak.job_intelligence.source.greehouse;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "job-sources.greenhouse")
public record GreenhouseProperties(String boardToken) {

}
