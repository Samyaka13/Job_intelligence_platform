package com.samyak.job_intelligence.job.service.parsing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SalaryParser {

    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "(?:₹|rs\\.?|inr\\s*)?"
                    + "(\\d+(?:\\.\\d+)?)"
                    + "\\s*(lpa|lakhs?|k)?"
                    + "\\s*(?:-|–|—|to)\\s*"
                    + "(\\d+(?:\\.\\d+)?)"
                    + "\\s*(lpa|lakhs?|k)?",
            Pattern.CASE_INSENSITIVE
    );

    public SalaryRange parse(String text) {

        if (text == null || text.isBlank()) {
            return new SalaryRange(null, null, null);
        }

        Matcher matcher = RANGE_PATTERN.matcher(text);

        while (matcher.find()) {

            String minUnit = matcher.group(2);
            String maxUnit = matcher.group(4);

            // Ignore ranges such as "2-4 years"
            String matchedText = matcher.group().toLowerCase(Locale.ROOT);

            if (matchedText.contains("year")
                    || (minUnit == null && maxUnit == null)) {
                continue;
            }

            BigDecimal minValue =
                    new BigDecimal(matcher.group(1));

            BigDecimal maxValue =
                    new BigDecimal(matcher.group(3));

            // If unit is present on only one side,
            // apply it to both values.
            if (minUnit == null) {
                minUnit = maxUnit;
            }

            if (maxUnit == null) {
                maxUnit = minUnit;
            }

            BigDecimal min = convert(minValue, minUnit);
            BigDecimal max = convert(maxValue, maxUnit);

            return new SalaryRange(
                    min,
                    max,
                    detectCurrency(text)
            );
        }

        return new SalaryRange(
                null,
                null,
                detectCurrency(text)
        );
    }

    private BigDecimal convert(BigDecimal value, String unit) {

        if (unit == null) {
            return value;
        }

        return switch (unit.toLowerCase(Locale.ROOT)) {

            case "lpa", "lakh", "lakhs" ->
                    value.multiply(BigDecimal.valueOf(100_000));

            case "k" ->
                    value.multiply(BigDecimal.valueOf(1_000));

            default ->
                    value;
        };
    }

    private String detectCurrency(String text) {

        String normalized = text.toLowerCase(Locale.ROOT);

        if (text.contains("₹")
                || normalized.contains("inr")
                || normalized.contains("rs.")) {
            return "INR";
        }

        return null;
    }
}