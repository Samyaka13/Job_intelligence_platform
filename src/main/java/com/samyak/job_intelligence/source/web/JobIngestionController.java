package com.samyak.job_intelligence.source.web;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.service.CompanyService;
import com.samyak.job_intelligence.job.service.ingestion.JobIngestionResult;
import com.samyak.job_intelligence.job.service.ingestion.JobIngestionService;
import com.samyak.job_intelligence.source.greehouse.GreenhouseCollector;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
public class JobIngestionController {
    private final CompanyService companyService;
    private final JobIngestionService jobIngestionService;
    private final GreenhouseCollector greenhouseCollector;


    public JobIngestionController(CompanyService companyService, JobIngestionService jobIngestionService, GreenhouseCollector greenhouseCollector) {
        this.companyService = companyService;
        this.jobIngestionService = jobIngestionService;
        this.greenhouseCollector = greenhouseCollector;
    }

    @PostMapping("/{companyId}/sources/greenhouse/ingest")
    public JobIngestionResult ingestGreenhouseJobs( @PathVariable Long companyId){
        Company company = companyService.getById(companyId);
        return jobIngestionService.ingest(
                companyId,
                company.getDisplayName(),
                greenhouseCollector
        );

    }
}
