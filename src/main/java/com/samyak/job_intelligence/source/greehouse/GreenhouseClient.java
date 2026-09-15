package com.samyak.job_intelligence.source.greehouse;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GreenhouseClient {
    private final RestClient restClient;

    public GreenhouseClient(RestClient.Builder restClientBuilder){
        this.restClient = restClientBuilder.baseUrl("https://boards-api.greenhouse.io").build();
    }

    public GreenhouseJobsResponse getJobs(String boardToken){
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/boards/{boardToken}/jobs")
                        .queryParam("content", true)
                        .build(boardToken))
                .retrieve()
                .body(GreenhouseJobsResponse.class);

    }
}
