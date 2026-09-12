package com.samyak.job_intelligence.job.service.ingestion;

public record JobIngestionResult(int collected, int created, int updated,int deactivated, int failed) {}
