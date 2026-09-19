package com.samyak.job_intelligence.job.service.repair;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/repairs")
public class JobDataRepairController {

    private final JobDataRepairService jobDataRepairService;

    public JobDataRepairController(
            JobDataRepairService jobDataRepairService
    ) {
        this.jobDataRepairService = jobDataRepairService;
    }

    @PostMapping("/greenhouse-merged-jobs")
    public String repairGreenhouseMergedJobs() {

        int repairedGroups =
                jobDataRepairService.repairGreenhouseMergedJobs();

        return "Repaired groups: " + repairedGroups;
    }
}