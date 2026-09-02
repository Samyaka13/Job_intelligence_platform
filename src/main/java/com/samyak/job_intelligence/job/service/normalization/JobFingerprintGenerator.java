package com.samyak.job_intelligence.job.service.normalization;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class JobFingerprintGenerator {

    public String generate(
            String normalizedCompanyName,
            String normalizedTitle,
            String normalizedLocation,
            String employmentType,
            String seniorityLevel,
            String experienceRange
    ) {
        String canonicalInput = String.join(
                "|",
                safe(normalizedCompanyName),
                safe(normalizedTitle),
                safe(normalizedLocation),
                safe(employmentType),
                safe(seniorityLevel),
                safe(experienceRange)
        );

        return sha256(canonicalInput);
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