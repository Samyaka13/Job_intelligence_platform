package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;

import java.util.List;

public interface JobSourceCollector {

    String getSource();
    List<RawJobListing> collect(SourceConfiguration sourceConfiguration);
}
