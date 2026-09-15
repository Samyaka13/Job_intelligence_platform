package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;

public interface JobSourceCollectorResolver {
    JobSourceCollector resolve(SourceConfiguration sourceConfiguration);
}
