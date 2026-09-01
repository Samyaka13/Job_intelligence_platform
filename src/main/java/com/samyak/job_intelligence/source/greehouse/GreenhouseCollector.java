package com.samyak.job_intelligence.source.greehouse;

import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GreenhouseCollector implements JobSourceCollector {
    private final GreenhouseClient greenhouseClient;
    private final GreenhouseJobMapper greenhouseJobMapper ;
    private static final String SOURCE_CODE = "GREENHOUSE";

    public GreenhouseCollector(GreenhouseClient greenhouseClient,GreenhouseJobMapper greenhouseJobMapper){
        this.greenhouseClient = greenhouseClient;
        this.greenhouseJobMapper = greenhouseJobMapper;
    }

    @Override
    public String getSource() {
        return SOURCE_CODE;
    }

    @Override
    public List<RawJobListing> collect() {
        GreenhouseJobsResponse greenhouseJobsResponse = greenhouseClient.getJobs();
        return  greenhouseJobsResponse.jobs().stream().map(job -> greenhouseJobMapper.map(job)).toList();
    }
}
