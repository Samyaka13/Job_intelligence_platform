package com.samyak.job_intelligence.source.web;

import com.samyak.job_intelligence.job.service.ingestion.JobIngestionResult;
import com.samyak.job_intelligence.source.service.CompanyIngestionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
public class JobIngestionController {
    private final CompanyIngestionService companyIngestionService;


    public JobIngestionController(CompanyIngestionService companyIngestionService) {
        this.companyIngestionService = companyIngestionService;
    }

    @PostMapping("/{companyId}/ingest")
    public List<JobIngestionResult> ingest(@PathVariable Long companyId){
        return companyIngestionService.ingest(companyId);
    }
}
