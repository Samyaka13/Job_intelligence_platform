package com.samyak.job_intelligence.source.greehouse;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record GreenhouseJobsResponse (List<JsonNode> jobs){
}
