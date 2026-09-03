package com.samyak.job_intelligence.job.service.parsing;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class SalaryParserTest {
    private final SalaryParser parser = new SalaryParser();

    @Test
    void shouldParseIndianLpaRange() {
        SalaryRange result = parser.parse("₹8-12 LPA");

        assertEquals(new BigDecimal("800000"), result.min());
        assertEquals(new BigDecimal("1200000"), result.max());
        assertEquals("INR", result.currency());
    }

    @Test
    void shouldParseLpaRangeWithoutCurrency() {
        SalaryRange result = parser.parse("8-12 LPA");

        assertEquals(new BigDecimal("800000"), result.min());
        assertEquals(new BigDecimal("1200000"), result.max());
        assertNull(result.currency());
    }

    @Test
    void shouldParseKRange() {
        SalaryRange result = parser.parse("80k-120k");

        assertEquals(new BigDecimal("80000"), result.min());
        assertEquals(new BigDecimal("120000"), result.max());
    }

    @Test
    void shouldReturnEmptySalaryWhenNotPresent() {
        SalaryRange result = parser.parse(
                "Competitive salary based on experience"
        );

        assertNull(result.min());
        assertNull(result.max());
        assertNull(result.currency());
    }
}
