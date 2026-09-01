package com.samyak.job_intelligence.job.service.normalization;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class JobUrlNormalizer {

    public String normalize(String url) {
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

            return new URI(
                    scheme,
                    uri.getUserInfo(),
                    host,
                    uri.getPort(),
                    normalizePath(uri.getPath()),
                    null,
                    null
            ).toString();

        } catch (URISyntaxException exception) {
            return trimmedUrl;
        }
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