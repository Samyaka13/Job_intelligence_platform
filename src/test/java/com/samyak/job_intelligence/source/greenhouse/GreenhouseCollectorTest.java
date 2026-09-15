package com.samyak.job_intelligence.source.greenhouse;

import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceType;
import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.greehouse.GreenhouseClient;
import com.samyak.job_intelligence.source.greehouse.GreenhouseCollector;
import com.samyak.job_intelligence.source.greehouse.GreenhouseJobMapper;
import com.samyak.job_intelligence.source.greehouse.GreenhouseJobsResponse;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GreenhouseCollectorTest {

    private final GreenhouseClient greenhouseClient =
            mock(GreenhouseClient.class);

    private final GreenhouseJobMapper greenhouseJobMapper =
            mock(GreenhouseJobMapper.class);

    private final GreenhouseCollector greenhouseCollector =
            new GreenhouseCollector(
                    greenhouseClient,
                    greenhouseJobMapper
            );

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldCollectJobsUsingBoardTokenAndMapThem() throws Exception {

        JsonNode job1 = objectMapper.readTree(
                """
                {
                  "id": "123",
                  "title": "Backend Engineer"
                }
                """
        );

        JsonNode job2 = objectMapper.readTree(
                """
                {
                  "id": "456",
                  "title": "Software Engineer"
                }
                """
        );

        GreenhouseJobsResponse response =
                new GreenhouseJobsResponse(
                        List.of(job1, job2)
                );

        RawJobListing listing1 =
                new RawJobListing(
                        "123",
                        "Backend Engineer",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        job1
                );

        RawJobListing listing2 =
                new RawJobListing(
                        "456",
                        "Software Engineer",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        job2
                );

        JobSource jobSource =
                new JobSource(
                        "GREENHOUSE",
                        "Greenhouse",
                        JobSourceType.ATS,
                        "https://boards.greenhouse.io"
                );

        SourceConfiguration sourceConfiguration =
                new SourceConfiguration(
                        null,
                        jobSource,
                        JsonNodeFactory.instance
                                .objectNode()
                                .put("boardToken", "airbnb"),
                        true
                );

        when(greenhouseClient.getJobs("airbnb"))
                .thenReturn(response);

        when(greenhouseJobMapper.map(job1))
                .thenReturn(listing1);

        when(greenhouseJobMapper.map(job2))
                .thenReturn(listing2);

        List<RawJobListing> result =
                greenhouseCollector.collect(sourceConfiguration);

        assertThat(result)
                .containsExactly(
                        listing1,
                        listing2
                );

        verify(greenhouseClient)
                .getJobs("airbnb");

        verify(greenhouseJobMapper)
                .map(job1);

        verify(greenhouseJobMapper)
                .map(job2);
    }

    @Test
    void shouldReturnGreenhouseAsSourceCode() {

        assertThat(greenhouseCollector.getSource())
                .isEqualTo("GREENHOUSE");
    }
}