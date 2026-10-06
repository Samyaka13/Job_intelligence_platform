package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import com.samyak.job_intelligence.candidate.service.CandidateProfileService;
import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobMatch;
import com.samyak.job_intelligence.job.repository.JobMatchRepository;
import com.samyak.job_intelligence.job.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class JobMatchPersistenceService {
    private final JobMatchRepository jobMatchRepository;
    private final JobService jobService;
    private final JobMatchPersistenceMapper jobMatchPersistenceMapper;
    private final CandidateProfileService candidateProfileService;


public JobMatchPersistenceService(JobMatchRepository jobMatchRepository, JobService jobService, JobMatchPersistenceMapper jobMatchPersistenceMapper,CandidateProfileService candidateProfileService) {
        this.jobMatchRepository = jobMatchRepository;
        this.jobService = jobService;
        this.jobMatchPersistenceMapper = jobMatchPersistenceMapper;
        this.candidateProfileService = candidateProfileService;
    }

    public JobMatch save(Long jobId, Long candidateProfileId, JobMatchResult jobMatchResult, Instant evaluatedAt){
        Job job = jobService.getById(jobId);
        CandidateProfile candidateProfile = candidateProfileService.getById(candidateProfileId);
        JobMatchPersistenceData  data = jobMatchPersistenceMapper.map(jobMatchResult,evaluatedAt);

//        JobMatch jobMatch = jobMatchRepository.findByJobIdAndCandidateProfileId(jobId,candidateProfileId).orElseGet(() ->
//                new JobMatch(
//                        job,
//                        candidateProfile,
//                        data.hardQualified(),
//                        data.skillScore(),
//                        data.experienceScore(),
//                        data.roleScore(),
//                        data.locationScore(),
//                        data.salaryScore(),
//                        data.semanticScore(),
//                        data.finalScore(),
//                        data.matchReasoning(),
//                        data.llmProvider(),
//                        data.llmModel(),
//                        data.llmPromptVersion(),
//                        data.evaluatedAt()
//                )
//        );

        Optional<JobMatch> existingJobMatch =
                jobMatchRepository.findByJobIdAndCandidateProfileId(
                        jobId,
                        candidateProfileId
                );



        if (existingJobMatch.isPresent()) {

            JobMatch jobMatch = existingJobMatch.get();

            jobMatch.update(
                    data.hardQualified(),
                    data.skillScore(),
                    data.experienceScore(),
                    data.roleScore(),
                    data.locationScore(),
                    data.salaryScore(),
                    data.semanticScore(),
                    data.finalScore(),
                    data.matchReasoning(),
                    data.llmProvider(),
                    data.llmModel(),
                    data.llmPromptVersion(),
                    data.evaluatedAt(),
                    data.mandatorySkills(),
                    data.missingMandatorySkills()
            );

            return jobMatchRepository.save(jobMatch);
        }
        JobMatch newJobMatch = new JobMatch(
                job,
                candidateProfile,
                data.hardQualified(),
                data.skillScore(),
                data.experienceScore(),
                data.roleScore(),
                data.locationScore(),
                data.salaryScore(),
                data.semanticScore(),
                data.finalScore(),
                data.matchReasoning(),
                data.llmProvider(),
                data.llmModel(),
                data.llmPromptVersion(),
                data.evaluatedAt(),
                data.mandatorySkills(),
                data.missingMandatorySkills()
        );

        return jobMatchRepository.save(newJobMatch);

    }
}
