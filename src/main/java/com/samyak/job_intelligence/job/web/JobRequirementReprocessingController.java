package com.samyak.job_intelligence.job.web;

import com.samyak.job_intelligence.job.service.requirement.JobRequirementReprocessingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobRequirementReprocessingController {

    private final JobRequirementReprocessingService service;

    public JobRequirementReprocessingController(
            JobRequirementReprocessingService service
    ) {
        this.service = service;
    }

    @PostMapping("/{jobId}/requirements/reprocess")
    public ReprocessResponse reprocess(@PathVariable Long jobId) {

        int requirementsCount =
                service.reprocess(jobId).size();

        return new ReprocessResponse(
                jobId,
                requirementsCount
        );
    }

    public record ReprocessResponse(
            Long jobId,
            int requirementsCount
    ) {
    }
}