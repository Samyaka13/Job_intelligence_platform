package com.samyak.job_intelligence.source.greehouse;

import com.samyak.job_intelligence.source.service.RawJobListing;
import com.samyak.job_intelligence.source.service.RawJobLocation;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Component
public class GreenhouseJobMapper {
    private List<RawJobLocation> mapLocations(JsonNode job){
        JsonNode location = job.get("location");
        if(location == null || location.isNull()){
            return List.of();
        }
        String displayName = getText(location,"name");

        if(displayName == null || displayName.isBlank()){
            return List.of();
        }

        return List.of(new RawJobLocation(null,null,null,displayName));
    }
public RawJobListing map(JsonNode job){
    return new RawJobListing(
            getText(job,"id"),
            getText(job,"title"),
            getText(job,"content"),
            getText(job,"url"),
            getText(job,"absolute_url"),
            mapLocations(job),
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
