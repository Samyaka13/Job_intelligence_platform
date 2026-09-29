package com.samyak.job_intelligence.job.service.parsing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExperienceParser {

    private static final Map<String, BigDecimal> NUMBER_WORDS = Map.ofEntries(
            Map.entry("one", BigDecimal.ONE),
            Map.entry("two", BigDecimal.valueOf(2)),
            Map.entry("three", BigDecimal.valueOf(3)),
            Map.entry("four", BigDecimal.valueOf(4)),
            Map.entry("five", BigDecimal.valueOf(5)),
            Map.entry("six", BigDecimal.valueOf(6)),
            Map.entry("seven", BigDecimal.valueOf(7)),
            Map.entry("eight", BigDecimal.valueOf(8)),
            Map.entry("nine", BigDecimal.valueOf(9)),
            Map.entry("ten", BigDecimal.TEN),
            Map.entry("eleven", BigDecimal.valueOf(11)),
            Map.entry("twelve", BigDecimal.valueOf(12)),
            Map.entry("thirteen", BigDecimal.valueOf(13)),
            Map.entry("fourteen", BigDecimal.valueOf(14)),
            Map.entry("fifteen", BigDecimal.valueOf(15)),
            Map.entry("sixteen", BigDecimal.valueOf(16)),
            Map.entry("seventeen", BigDecimal.valueOf(17)),
            Map.entry("eighteen", BigDecimal.valueOf(18)),
            Map.entry("nineteen", BigDecimal.valueOf(19)),
            Map.entry("twenty", BigDecimal.valueOf(20))
    );

    /*
     * Handles overall experience statements such as:
     *
     * "plus six (6) years of software development experience"
     * "must have 6 years of professional experience"
     * "requires 5 years of experience"
     *
     * The negative lookahead prevents us from treating statements like:
     *
     * "four (4) years of experience in each of the following"
     *
     * as overall job experience.
     */
    private static final Pattern OVERALL_EXPERIENCE_PATTERN =
            Pattern.compile(
                    "(?:^|[.;]\\s*|\\b(?:must\\s+(?:also\\s+)?have|requires?|needs?|plus|at\\s+least|minimum(?:\\s+of)?)\\s+)"
                            + "(\\d+(?:\\.\\d+)?|one|two|three|four|five|six|seven|eight|nine|ten"
                            + "|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen"
                            + "|nineteen|twenty)"
                            + "\\s*(?:\\(\\s*(\\d+(?:\\.\\d+)?)\\s*\\))?"
                            + "\\s*(?:years?|yrs?)"
                            + "\\s+of\\s+"
                            + "(?:(?:[a-z]+\\s+){0,6})?"
                            + "experience\\b"
                            + "(?!\\s+(?:in|with|for|per|each)\\b)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(?:-|–|—|to)\\s*(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MIN_PATTERN = Pattern.compile(
            "(?:at least|minimum(?: of)?|min(?:imum)?)\\s*(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)"
                    + "|(\\d+(?:\\.\\d+)?)\\s*\\+\\s*(?:years?|yrs?)"
                    + "|(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)\\s*(?:minimum|min)",
            Pattern.CASE_INSENSITIVE
    );

    public ExperienceRange parse(String text) {

        if (text == null || text.isBlank()) {
            return new ExperienceRange(null, null);
        }

        /*
         * 1. Prefer an explicit overall experience statement.
         */
        Matcher overallMatcher =
                OVERALL_EXPERIENCE_PATTERN.matcher(text);

        if (overallMatcher.find()) {

            BigDecimal years =
                    parseNumber(
                            overallMatcher.group(1),
                            overallMatcher.group(2)
                    );

            return new ExperienceRange(years, null);
        }

        /*
         * 2. Existing numeric range support.
         */
        Matcher rangeMatcher = RANGE_PATTERN.matcher(text);

        if (rangeMatcher.find()) {
            return new ExperienceRange(
                    new BigDecimal(rangeMatcher.group(1)),
                    new BigDecimal(rangeMatcher.group(2))
            );
        }

        /*
         * 3. Existing minimum experience support.
         */
        Matcher minMatcher = MIN_PATTERN.matcher(text);

        if (minMatcher.find()) {

            String value =
                    minMatcher.group(1) != null
                            ? minMatcher.group(1)
                            : minMatcher.group(2) != null
                            ? minMatcher.group(2)
                            : minMatcher.group(3);

            return new ExperienceRange(
                    new BigDecimal(value),
                    null
            );
        }

        return new ExperienceRange(null, null);
    }

    private BigDecimal parseNumber(
            String wordOrNumber,
            String parenthesizedNumber
    ) {

        /*
         * Prefer the explicit numeric value inside parentheses:
         *
         * "six (6)" -> 6
         */
        if (parenthesizedNumber != null) {
            return new BigDecimal(parenthesizedNumber);
        }

        if (wordOrNumber.matches("\\d+(?:\\.\\d+)?")) {
            return new BigDecimal(wordOrNumber);
        }

        BigDecimal value =
                NUMBER_WORDS.get(wordOrNumber.toLowerCase());

        if (value == null) {
            throw new IllegalArgumentException(
                    "Unsupported experience number: " + wordOrNumber
            );
        }

        return value;
    }
}