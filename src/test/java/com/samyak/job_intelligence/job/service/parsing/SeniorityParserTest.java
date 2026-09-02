package com.samyak.job_intelligence.job.service.parsing;

import com.samyak.job_intelligence.job.domain.SeniorityLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeniorityParserTest {

    private final SeniorityParser parser = new SeniorityParser();

    @Test
    void shouldParseIntern() {
        assertEquals(
                SeniorityLevel.INTERN,
                parser.parse("Software Engineering Intern")
        );
    }

    @Test
    void shouldParseJunior() {
        assertEquals(
                SeniorityLevel.JUNIOR,
                parser.parse("Junior Software Engineer")
        );
    }

    @Test
    void shouldParseMid() {
        assertEquals(
                SeniorityLevel.MID,
                parser.parse("Mid-level Software Engineer")
        );
    }

    @Test
    void shouldParseSenior() {
        assertEquals(
                SeniorityLevel.SENIOR,
                parser.parse("Senior Backend Engineer")
        );
    }

    @Test
    void shouldParseLead() {
        assertEquals(
                SeniorityLevel.LEAD,
                parser.parse("Tech Lead")
        );
    }

    @Test
    void shouldParseStaff() {
        assertEquals(
                SeniorityLevel.STAFF,
                parser.parse("Staff Software Engineer")
        );
    }

    @Test
    void shouldParseEntryLevel() {
        assertEquals(
                SeniorityLevel.ENTRY,
                parser.parse("Entry-level Software Engineer")
        );
    }

    @Test
    void shouldReturnUnknownForUnrecognizedText() {
        assertEquals(
                SeniorityLevel.UNKNOWN,
                parser.parse("Software Engineer")
        );
    }

    @Test
    void shouldReturnUnknownForNull() {
        assertEquals(
                SeniorityLevel.UNKNOWN,
                parser.parse(null)
        );
    }
}