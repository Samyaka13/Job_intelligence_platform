package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.greehouse.GreenhouseCollector;
import org.springframework.stereotype.Component;

@Component
public class DefaultJobSourceCollectorResolver implements JobSourceCollectorResolver {
    private final GreenhouseCollector greenhouseCollector;


    public DefaultJobSourceCollectorResolver(GreenhouseCollector greenhouseCollector) {
        this.greenhouseCollector = greenhouseCollector;
    }

    @Override
    public JobSourceCollector resolve(SourceConfiguration sourceConfiguration) {
        return switch (sourceConfiguration.getJobSource().getCode()){
            case "GREENHOUSE" -> greenhouseCollector;
            default -> throw new IllegalArgumentException(
                    "No collector configured for source: "
                            + sourceConfiguration.getJobSource().getCode()
            );
        };
    }
}
