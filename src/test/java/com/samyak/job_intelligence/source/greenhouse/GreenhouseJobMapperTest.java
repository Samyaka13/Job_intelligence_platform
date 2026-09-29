package com.samyak.job_intelligence.source.greenhouse;

import com.samyak.job_intelligence.source.greehouse.GreenhouseJobMapper;
import com.samyak.job_intelligence.source.service.RawJobListing;
import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreenhouseJobMapperTest {

    private final GreenhouseJobMapper greenhouseJobMapper =
            new GreenhouseJobMapper();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldMapCityStateAndCountryFromLocation() throws Exception {

        JsonNode job = objectMapper.readTree("""
        {
          "id": 123,
          "title": "Software Engineer",
          "content": "<p>Build great software.</p>",
          "url": "https://boards.greenhouse.io/example/jobs/123",
          "absolute_url": "https://example.com/jobs/123",
          "location": {
            "name": "Bangalore, Karnataka, India"
          }
        }
        """);

        RawJobListing result = greenhouseJobMapper.map(job);

        assertEquals(1, result.locations().size());

        assertEquals(
                "Bangalore",
                result.locations().getFirst().city()
        );

        assertEquals(
                "Karnataka",
                result.locations().getFirst().state()
        );

        assertEquals(
                "India",
                result.locations().getFirst().country()
        );

        assertEquals(
                "Bangalore, Karnataka, India",
                result.locations().getFirst().displayText()
        );
    }

    @Test
    void shouldMapCityAndStateWhenCountryIsNotProvided() throws Exception {

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
                "San Francisco",
                result.locations().getFirst().city()
        );

        assertEquals(
                "CA",
                result.locations().getFirst().state()
        );

        assertNull(
                result.locations().getFirst().country()
        );

        assertEquals(
                "San Francisco, CA",
                result.locations().getFirst().displayText()
        );
    }

    @Test
    void shouldReturnEmptyLocationsWhenLocationIsMissing() throws Exception {

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

    @Test
    void shouldMapCityAndCountryWhenCountryIsProvided() throws Exception {

        JsonNode job = objectMapper.readTree("""
    {
      "id": 123,
      "title": "Software Engineer",
      "content": "<p>Build great software.</p>",
      "absolute_url": "https://example.com/jobs/123",
      "location": {
        "name": "Paris, France"
      }
    }
    """);

        RawJobListing result = greenhouseJobMapper.map(job);

        assertEquals(1, result.locations().size());

        RawJobLocation location =
                result.locations().getFirst();

        assertEquals("Paris", location.city());
        assertEquals("France", location.country());
        assertNull(location.state());
        assertEquals("Paris, France", location.displayText());
    }

    @Test
    void shouldMapCountryWhenLocationContainsOnlyCountry() throws Exception {

        JsonNode job = objectMapper.readTree("""
    {
      "id": 123,
      "title": "Software Engineer",
      "content": "<p>Build great software.</p>",
      "absolute_url": "https://example.com/jobs/123",
      "location": {
        "name": "Brazil"
      }
    }
    """);

        RawJobListing result = greenhouseJobMapper.map(job);

        assertEquals(1, result.locations().size());

        RawJobLocation location =
                result.locations().getFirst();

        assertNull(location.city());
        assertNull(location.state());
        assertEquals("Brazil", location.country());
        assertEquals("Brazil", location.displayText());
    }

    @Test
    void shouldMapStructuredLocationFromGreenhouseJob() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode job = objectMapper.readTree("""
        {
          "id": "123",
          "title": "Acquisition Manager",
          "content": "Test content",
          "absolute_url": "https://example.com/job/123",
          "first_published": "2026-09-27T10:00:00Z",
          "location": {
            "name": "Paris, France"
          }
        }
        """);

        RawJobListing result = greenhouseJobMapper.map(job);

        assertThat(result.locations())
                .hasSize(1);

        RawJobLocation location =
                result.locations().getFirst();

        assertThat(location.city())
                .isEqualTo("Paris");

        assertThat(location.state())
                .isNull();

        assertThat(location.country())
                .isEqualTo("France");

        assertThat(location.displayText())
                .isEqualTo("Paris, France");
    }
}