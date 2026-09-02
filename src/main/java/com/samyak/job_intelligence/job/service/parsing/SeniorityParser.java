package com.samyak.job_intelligence.job.service.parsing;

import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import org.springframework.stereotype.Component;

@Component
public class SeniorityParser {

    public SeniorityLevel parse(String text) {
        if (text == null || text.isBlank()) {
            return SeniorityLevel.UNKNOWN;
        }

        String normalized = text.toLowerCase();

        if (normalized.contains("intern")
                || normalized.contains("internship")) {
            return SeniorityLevel.INTERN;
        }

        if (normalized.contains("staff")) {
            return SeniorityLevel.STAFF;
        }

        if (normalized.contains("lead")
                || normalized.contains("principal")) {
            return SeniorityLevel.LEAD;
        }

        if (normalized.contains("senior")
                || normalized.contains("sr.")) {
            return SeniorityLevel.SENIOR;
        }

        if (normalized.contains("junior")
                || normalized.contains("jr.")) {
            return SeniorityLevel.JUNIOR;
        }

        if (normalized.contains("mid-level")
                || normalized.contains("mid level")
                || normalized.contains("midlevel")) {
            return SeniorityLevel.MID;
        }

        if (normalized.contains("entry-level")
                || normalized.contains("entry level")
                || normalized.contains("graduate")
                || normalized.contains("fresher")) {
            return SeniorityLevel.ENTRY;
        }

        return SeniorityLevel.UNKNOWN;
    }
}