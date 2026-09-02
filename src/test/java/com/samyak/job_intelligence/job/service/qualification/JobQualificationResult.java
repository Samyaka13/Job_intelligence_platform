package com.samyak.job_intelligence.job.service.qualification;

import java.util.List;

public record JobQualificationResult (boolean qualified, List<String> rejectionReasons){

    public static JobQualificationResult qualifiedListing(){
        return new JobQualificationResult(true,List.of());
    }

    public static JobQualificationResult rejectedListing(List<String> reasons){
        return new JobQualificationResult(false,List.copyOf(reasons));
    }
}
