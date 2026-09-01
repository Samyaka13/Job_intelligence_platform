package com.samyak.job_intelligence.source.greehouse;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GreenhouseClient {
    private final RestClient restClient;
    private final GreenhouseProperties greenhouseProperties;

    public GreenhouseClient(RestClient.Builder restClientBuilder,GreenhouseProperties greenhouseProperties){
        this.restClient = restClientBuilder.baseUrl("https://boards-api.greenhouse.io").build();
        this.greenhouseProperties = greenhouseProperties;
    }

    public GreenhouseJobsResponse getJobs(){
        return restClient.get()
                .uri("/v1/boards/{boardToken}/jobs",greenhouseProperties.boardToken())
                .retrieve()
                .body(GreenhouseJobsResponse.class);

    }
}
