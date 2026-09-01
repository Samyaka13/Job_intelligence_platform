package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.source.domain.JobSource;

import java.util.List;

public interface JobSourceCollector {

    JobSource getSource();
    List<RawJobListing> collect();
}
