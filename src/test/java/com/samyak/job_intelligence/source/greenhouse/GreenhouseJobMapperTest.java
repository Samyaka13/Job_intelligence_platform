package com.samyak.job_intelligence.source.greenhouse;

import com.samyak.job_intelligence.source.greehouse.GreenhouseJobMapper;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.junit.jupiter.api.Test;



import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreenhouseJobMapperTest {

    private final GreenhouseJobMapper greenhouseJobMapper = new GreenhouseJobMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMapLocation() throws Exception {
        JsonNode job = objectMapper.readTree("""
        {
          "id": 123,
          "title": "Software Engineer",
          "content": "<p>Build great software.</p>",
          "url": "https://boards.greenhouse.io/example/jobs/123",
          "absolute_url": "https://example.com/jobs/123",
          "location": {
            "name": "San Francisco, CA"
          }
        }
        """);

        RawJobListing result = greenhouseJobMapper.map(job);

        assertEquals(1, result.locations().size());
        assertEquals(
                "San Francisco, CA",
                result.locations().getFirst().displayText()
        );
    }

    @Test
    void shouldReturnEmptyLocationsWhenLocationIsMissing() {
        JsonNode job = objectMapper.readTree("""
        {
          "id": 123,
          "title": "Software Engineer",
          "content": "<p>Build great software.</p>",
          "url": "https://boards.greenhouse.io/example/jobs/123",
          "absolute_url": "https://example.com/jobs/123"
        }
        """);

        RawJobListing result = greenhouseJobMapper.map(job);

        assertTrue(result.locations().isEmpty());
    }
}
