package com.samyak.job_intelligence.source.greehouse;

import com.samyak.job_intelligence.source.service.RawJobListing;
import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class GreenhouseJobMapper {

    private List<RawJobLocation> mapLocations(JsonNode job) {

        JsonNode location = job.get("location");

        if (location == null || location.isNull()) {
            return List.of();
        }

        String displayName = getText(location, "name");

        if (displayName == null || displayName.isBlank()) {
            return List.of();
        }

        return List.of(parseLocation(displayName));
    }

    private RawJobLocation parseLocation(String displayName) {

        String[] parts = displayName.split(",");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        if (parts.length == 1) {

            String value = parts[0];

            if (isCountry(value)) {
                return new RawJobLocation(
                        null,
                        null,
                        value,
                        displayName
                );
            }

            return new RawJobLocation(
                    value,
                    null,
                    null,
                    displayName
            );
        }

        if (parts.length == 2) {

            String first = parts[0];
            String second = parts[1];

            if (isCountry(second)) {
                return new RawJobLocation(
                        first,
                        null,
                        second,
                        displayName
                );
            }

            return new RawJobLocation(
                    first,
                    second,
                    null,
                    displayName
            );
        }

        if (parts.length == 3) {
            return new RawJobLocation(
                    parts[0],
                    parts[1],
                    parts[2],
                    displayName
            );
        }

        return new RawJobLocation(
                null,
                null,
                null,
                displayName
        );
    }

    public RawJobListing map(JsonNode job) {

        return new RawJobListing(
                getText(job, "id"),
                getText(job, "title"),
                getText(job, "content"),
                getText(job, "absolute_url"),
                getText(job, "absolute_url"),
                mapLocations(job),
                parseInstant(getText(job, "first_published")),
                job
        );
    }

    private String getText(JsonNode node, String fieldName) {

        JsonNode field = node.get(fieldName);

        if (field == null || field.isNull()) {
            return null;
        }

        return field.asString();
    }

    private boolean isCountry(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.trim();

        return Arrays.stream(Locale.getISOCountries())
                .map(code -> new Locale("", code)
                        .getDisplayCountry(Locale.ENGLISH))
                .anyMatch(country ->
                        country.equalsIgnoreCase(normalized)
                )
                || Set.of(
                        "Korea",
                        "United States"
                ).stream()
                .anyMatch(country ->
                        country.equalsIgnoreCase(normalized)
                );
    }

    private Instant parseInstant(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Instant.parse(
                OffsetDateTime.parse(value).toInstant().toString()
        );
    }
}