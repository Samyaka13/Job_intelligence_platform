package com.samyak.job_intelligence.job.service.normalization;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JobFingerprintGenerator {

    public String generate(
            String normalizedCompanyName,
            String normalizedTitle,
            List<NormalizedJobLocation> normalizedLocation,
            EmploymentType employmentType,
            SeniorityLevel seniorityLevel,
            BigDecimal minExperienceYears,
            BigDecimal maxExperienceYears
    ) {


        String normalizedLocations = normalizedLocation == null
                ? ""
                : normalizedLocation.stream()
                .map(this::normalizeLocation)
                .sorted()
                .collect(Collectors.joining(","));
        String canonicalInput = String.join(
                "|",
                safe(normalizedCompanyName),
                safe(normalizedTitle),
                safe(normalizedLocations),
                employmentType == null ? "" : employmentType.name(),
                seniorityLevel == null ? "" : seniorityLevel.name(),
                minExperienceYears == null ? "" : minExperienceYears.toString(),
                maxExperienceYears == null ? "" : maxExperienceYears.toString()
        );

        return sha256(canonicalInput);
    }
//This function is made like if the location city location state or location country is not present then we consider display name and if any of them is present then we ignore display text so that redundant data does go inside the fingerprint generated
    private String normalizeLocation(NormalizedJobLocation location) {
        boolean hasStructuredLocation =
                (location.city() != null && !location.city().isBlank())
                        || (location.state() != null && !location.state().isBlank())
                        || (location.country() != null && !location.country().isBlank());

        if (hasStructuredLocation) {
            return Stream.of(
                            location.city(),
                            location.state(),
                            location.country()
                    )
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(","));
        }

        return safe(location.displayText()).trim().toLowerCase();
    }
    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }


}