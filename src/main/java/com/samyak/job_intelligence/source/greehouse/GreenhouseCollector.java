package com.samyak.job_intelligence.source.greehouse;

import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.service.JobSourceCollector;
import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Component
public class GreenhouseCollector implements JobSourceCollector {
    private final GreenhouseClient greenhouseClient;
    private final GreenhouseJobMapper greenhouseJobMapper ;
    private static final String SOURCE_CODE = "GREENHOUSE";
    private static final String BOARD_TOKEN = "boardToken";

    public GreenhouseCollector(GreenhouseClient greenhouseClient,GreenhouseJobMapper greenhouseJobMapper){
        this.greenhouseClient = greenhouseClient;
        this.greenhouseJobMapper = greenhouseJobMapper;
    }

    @Override
    public String getSource() {
        return SOURCE_CODE;
    }

    @Override
    public List<RawJobListing> collect(SourceConfiguration sourceConfiguration) {
        JsonNode boardTokenNode = sourceConfiguration.getConfiguration().get(BOARD_TOKEN);
        if (boardTokenNode == null
                || !boardTokenNode.isString()
                || boardTokenNode.asString().isBlank()) {
            throw new IllegalArgumentException(
                    "Missing Greenhouse boardToken in source configuration"
            );
        }
        String boardToken = boardTokenNode.asString();

        GreenhouseJobsResponse greenhouseJobsResponse = greenhouseClient.getJobs(boardToken);
        return  greenhouseJobsResponse.jobs().stream().map(job -> greenhouseJobMapper.map(job)).toList();
    }
}
