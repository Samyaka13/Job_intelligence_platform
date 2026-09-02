package com.samyak.job_intelligence.job.service.parsing;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import org.springframework.stereotype.Component;

@Component
public class EmploymentTypeParser {

    public EmploymentType parse(String text){
        if(text == null || text.isBlank()){
            return EmploymentType.UNKNOWN;
        }
        String normalized = text.toLowerCase();
        if(normalized.contains("internship") || normalized.contains("intern")){
            return EmploymentType.INTERNSHIP;
        }
        if (normalized.contains("part-time")
                || normalized.contains("part time")) {
            return EmploymentType.PART_TIME;
        }

        if (normalized.contains("full-time")
                || normalized.contains("full time")) {
            return EmploymentType.FULL_TIME;
        }

        if (normalized.contains("contract")) {
            return EmploymentType.CONTRACT;
        }

        if (normalized.contains("temporary")
                || normalized.contains("temp ")) {
            return EmploymentType.TEMPORARY;
        }
        return  EmploymentType.UNKNOWN;
    }
}
