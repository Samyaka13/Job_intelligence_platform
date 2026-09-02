package com.samyak.job_intelligence.job.service.parsing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExperienceParserTest {

    private final ExperienceParser parser = new ExperienceParser();

    @Test
    void shouldParseExperienceRange() {

        ExperienceRange result =
                parser.parse("2-4 years of experience");

        assertThat(result.minYears())
                .isEqualByComparingTo(BigDecimal.valueOf(2));

        assertThat(result.maxYears())
                .isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    void shouldParseExperienceRangeWithTo() {

        ExperienceRange result =
                parser.parse("2 to 4 years");

        assertThat(result.minYears())
                .isEqualByComparingTo(BigDecimal.valueOf(2));

        assertThat(result.maxYears())
                .isEqualByComparingTo(BigDecimal.valueOf(4));
    }

//    @Test
//    void shouldParseMinimumExperience() {
//
//        ExperienceRange result =
//                parser.parse("at least 2 years of experience");
//
//        assertThat(result.minYears())
//                .isEqualByComparingTo(BigDecimal.valueOf(2));
//
//        assertThat(result.maxYears())
//                .isNull();
//    }

    @Test
    void shouldReturnEmptyRangeWhenExperienceIsNotPresent() {

        ExperienceRange result =
                parser.parse("Experience with Java and Spring Boot.");

        assertThat(result.minYears()).isNull();
        assertThat(result.maxYears()).isNull();
    }

    @Test
    void shouldHandleNullInput() {

        ExperienceRange result = parser.parse(null);

        assertThat(result.minYears()).isNull();
        assertThat(result.maxYears()).isNull();
    }

    @Test
    void shouldHandleDecimalExperience() {

        ExperienceRange result =
                parser.parse("1.5 - 3.5 years");

        assertThat(result.minYears())
                .isEqualByComparingTo(BigDecimal.valueOf(1.5));

        assertThat(result.maxYears())
                .isEqualByComparingTo(BigDecimal.valueOf(3.5));
    }

    @Test
    void shouldParsePlusExperience() {
        ExperienceRange result = parser.parse("2+ years");

        assertEquals(new BigDecimal("2"), result.minYears());
        assertNull(result.maxYears());
    }

    @Test
    void shouldParseMinimumExperience() {
        ExperienceRange result = parser.parse("3 years minimum");

        assertEquals(new BigDecimal("3"), result.minYears());
        assertNull(result.maxYears());
    }

    @Test
    void shouldParseMinimumOfExperience() {
        ExperienceRange result = parser.parse("minimum of 4 years");

        assertEquals(new BigDecimal("4"), result.minYears());
        assertNull(result.maxYears());
    }
}