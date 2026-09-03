package com.samyak.job_intelligence.job.service.parsing;

import java.math.BigDecimal;

public record SalaryRange(
        BigDecimal min,
        BigDecimal max,
        String currency
) {
}