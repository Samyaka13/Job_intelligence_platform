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
        System.out.println("GREENHOUSE_BOARD_TOKEN from OS: "
                + System.getenv("GREENHOUSE_BOARD_TOKEN"));

        System.out.println("GREENHOUSE_BOARD_TOKEN from Spring: "
                + greenhouseProperties.boardToken());
    }

    public GreenhouseJobsResponse getJobs(){
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/boards/{boardToken}/jobs")
                        .queryParam("content", true)
                        .build(greenhouseProperties.boardToken()))
                .retrieve()
                .body(GreenhouseJobsResponse.class);

    }
}
