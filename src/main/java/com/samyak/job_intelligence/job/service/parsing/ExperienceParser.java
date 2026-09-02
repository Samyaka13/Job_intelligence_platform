package com.samyak.job_intelligence.job.service.parsing;


import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExperienceParser {
    private final static Pattern RANGE_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(?:-|–|—|to)\\s*(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MIN_PATTERN = Pattern.compile(
            "(?:at least|minimum(?: of)?|min(?:imum)?)\\s*(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)"
                    + "|(\\d+(?:\\.\\d+)?)\\s*\\+\\s*(?:years?|yrs?)"
                    + "|(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)\\s*(?:minimum|min)",
            Pattern.CASE_INSENSITIVE
    );

    public ExperienceRange parse(String text){
        if( text == null || text.isBlank() ){
            return new ExperienceRange(null,null);
        }

        Matcher rangeMatcher = RANGE_PATTERN.matcher(text);
        if(rangeMatcher.find()){
            return new ExperienceRange(
                    new BigDecimal(rangeMatcher.group(1)),
                    new BigDecimal(rangeMatcher.group(2))
            );
        }

        Matcher minMatcher = MIN_PATTERN.matcher(text);

        if (minMatcher.find()) {
            String value = minMatcher.group(1) != null
                    ? minMatcher.group(1)
                    : minMatcher.group(2) != null
                    ? minMatcher.group(2)
                    : minMatcher.group(3);

            return new ExperienceRange(
                    new BigDecimal(value),
                    null
            );
        }

        return new ExperienceRange(null,null);
    }
}
