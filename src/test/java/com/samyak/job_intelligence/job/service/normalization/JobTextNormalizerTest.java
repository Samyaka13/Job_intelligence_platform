package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobTextNormalizerTest {
    private final JobTextNormalizer normalizer = new JobTextNormalizer();

    @Test
    void shouldNormalizeJobLocations() {

        List<RawJobLocation> rawLocations = List.of(
                new RawJobLocation(
                        "  Bangalore  ",
                        "  Karnataka ",
                        "  India ",
                        "  Bangalore, Karnataka, India  "
                ),
                new RawJobLocation(
                        "Hyderabad",
                        "Telangana",
                        "India",
                        "Hyderabad, Telangana, India"
                )
        );

        List<NormalizedJobLocation> result =
                normalizer.normalizeJobLocations(rawLocations);

        assertEquals(2, result.size());

        assertEquals("bangalore", result.get(0).city());
        assertEquals("karnataka", result.get(0).state());
        assertEquals("india", result.get(0).country());
        assertEquals(
                "bangalore, karnataka, india",
                result.get(0).displayText()
        );

        assertEquals("hyderabad", result.get(1).city());
    }

    @Test
    void shouldReturnEmptyListWhenJobLocationsAreNull() {
        assertTrue(
                normalizer.normalizeJobLocations(null).isEmpty()
        );
    }

}
