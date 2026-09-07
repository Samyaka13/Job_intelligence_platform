package com.samyak.job_intelligence.job.service;


import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobLocation;
import com.samyak.job_intelligence.job.domain.RemoteType;
import com.samyak.job_intelligence.job.repository.JobLocationRepository;
import com.samyak.job_intelligence.job.service.normalization.NormalizedJobLocation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobLocationService {
    private final JobLocationRepository jobLocationRepository;
    private final JobService jobService;


    public JobLocationService(JobLocationRepository jobLocationRepository, JobService jobService) {
        this.jobLocationRepository = jobLocationRepository;
        this.jobService = jobService;
    }

    public void replaceLocations(Long jobId, List<NormalizedJobLocation> normalizedJobLocationList){
        Job job = jobService.getById(jobId);
        jobLocationRepository.deleteAll(jobLocationRepository.findByJobId(jobId));

        if(normalizedJobLocationList == null || normalizedJobLocationList.isEmpty()){
            return;
        }

        List<JobLocation> locations = normalizedJobLocationList.stream().
                map(location ->new JobLocation(job,location.city(),location.state(),location.country(), RemoteType.UNKNOWN,location.displayText()))
                .toList();

        jobLocationRepository.saveAll(locations);

    }
}
