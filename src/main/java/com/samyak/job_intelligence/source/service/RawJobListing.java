package com.samyak.job_intelligence.source.service;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

public record RawJobListing(
        String externalJobId, String title, String description, String sourceUrl, String applicationUrl,
                            List<RawJobLocation> locations , Instant postedAt, JsonNode rawPayload) {}
