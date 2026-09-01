package com.samyak.job_intelligence.source.greenhouse;

import com.samyak.job_intelligence.source.greehouse.GreenhouseClient;
import com.samyak.job_intelligence.source.greehouse.GreenhouseJobsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GreenhouseClientIT {

    @Autowired
    private GreenhouseClient greenhouseClient;

    @Test
    void shouldFetchAirbnbJobs() {

        GreenhouseJobsResponse response = greenhouseClient.getJobs();

        assertThat(response).isNotNull();
        assertThat(response.jobs()).isNotNull();
        assertThat(response.jobs()).isNotEmpty();
    }
}