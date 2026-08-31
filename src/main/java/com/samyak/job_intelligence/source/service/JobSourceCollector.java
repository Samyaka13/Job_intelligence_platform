package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.source.domain.JobSource;

public interface JobSourceCollector {

    JobSource getSource();
//    List<RawJobListing> collect();
}
