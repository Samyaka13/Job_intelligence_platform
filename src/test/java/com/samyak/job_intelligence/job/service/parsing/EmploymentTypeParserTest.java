package com.samyak.job_intelligence.job.service.parsing;

import com.samyak.job_intelligence.job.domain.EmploymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmploymentTypeParserTest {

    private final EmploymentTypeParser parser = new EmploymentTypeParser();

    @Test
    void shouldParseFullTime() {
        assertEquals(
                EmploymentType.FULL_TIME,
                parser.parse("Full-time Software Engineer")
        );
    }

    @Test
    void shouldParsePartTime() {
        assertEquals(
                EmploymentType.PART_TIME,
                parser.parse("Part-time Developer")
        );
    }

    @Test
    void shouldParseContract() {
        assertEquals(
                EmploymentType.CONTRACT,
                parser.parse("6 month contract role")
        );
    }

    @Test
    void shouldParseInternship() {
        assertEquals(
                EmploymentType.INTERNSHIP,
                parser.parse("Software Engineering Internship")
        );
    }

    @Test
    void shouldParseTemporary() {
        assertEquals(
                EmploymentType.TEMPORARY,
                parser.parse("Temporary Software Engineer")
        );
    }

    @Test
    void shouldReturnUnknownForUnrecognizedText() {
        assertEquals(
                EmploymentType.UNKNOWN,
                parser.parse("Software Engineer")
        );
    }

    @Test
    void shouldReturnUnknownForNull() {
        assertEquals(
                EmploymentType.UNKNOWN,
                parser.parse(null)
        );
    }
}