package com.samyak.job_intelligence.source.greehouse;

import com.samyak.job_intelligence.source.service.RawJobListing;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class GreenhouseJobMapper {
public RawJobListing map(JsonNode job){
    return new RawJobListing(
            getText(job,"id"),
            getText(job,"title"),
            getText(job,"content"),
            getText(job,"url"),
            getText(job,"absolute_url"),
            null,
            job
    );
}

private String getText(JsonNode node,String fieldName){
    JsonNode field = node.get(fieldName);
    if(field == null || field.isNull() ) return null;
    return field.asString();
}
}
