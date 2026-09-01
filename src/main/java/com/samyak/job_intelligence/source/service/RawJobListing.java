package com.samyak.job_intelligence.source.service;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record RawJobListing(String externalJobId, String title, String description, String sourceUrl, String applicationUrl,
                            Instant postedAt, JsonNode rawPayload) {}
