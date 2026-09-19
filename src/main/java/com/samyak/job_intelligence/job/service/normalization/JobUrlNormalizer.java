package com.samyak.job_intelligence.job.service.normalization;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JobUrlNormalizer {
    private static final Map<String, Set<String>> IDENTITY_PARAMETERS_BY_SOURCE =
            Map.of(
                    "GREENHOUSE", Set.of("gh_jid")
            );
    public String normalize(String url,String sourceCode) {

        if (url == null || url.isBlank()) {
            return null;
        }

        String trimmedUrl = url.trim();

        try {
            URI uri = new URI(trimmedUrl);

            String scheme = uri.getScheme() == null
                    ? null
                    : uri.getScheme().toLowerCase();

            String host = uri.getHost() == null
                    ? null
                    : uri.getHost().toLowerCase();

            if (scheme == null || host == null) {
                return trimmedUrl;
            }

            String normalizedQuery =
                    normalizeQuery(uri.getRawQuery(), sourceCode);
            return new URI(
                    scheme,
                    uri.getUserInfo(),
                    host,
                    uri.getPort(),
                    normalizePath(uri.getPath()),
                    normalizedQuery,
                    null
            ).toString();

        } catch (URISyntaxException exception) {
            return trimmedUrl;
        }
    }

    private String normalizeQuery(String query, String sourceCode) {
        if (query == null || query.isBlank()) {
            return null;
        }

        Set<String> allowedParameters =
                IDENTITY_PARAMETERS_BY_SOURCE.getOrDefault(
                        sourceCode.toUpperCase(),
                        Set.of()
                );

        if (allowedParameters.isEmpty()) {
            return null;
        }

        return java.util.Arrays.stream(query.split("&"))
                .filter(parameter -> {
                    String parameterName =
                            parameter.contains("=")
                                    ? parameter.substring(0, parameter.indexOf("="))
                                    : parameter;

                    return allowedParameters.contains(parameterName);
                })
                .collect(Collectors.joining("&"));
    }
    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }
}