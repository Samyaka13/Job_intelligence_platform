package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.candidate.repository.CandidateProfileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyMatchingScheduler {
    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateJobMatchingService candidateJobMatchingService;


    public DailyMatchingScheduler(CandidateProfileRepository candidateProfileRepository, CandidateJobMatchingService candidateJobMatchingService) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.candidateJobMatchingService = candidateJobMatchingService;
    }

    @Scheduled(cron = "${job-intelligence.matching.daily-cron}")
    public void runDailyMatching(){
        List<CandidateProfile> candidates = candidateProfileRepository.findByIsActiveTrue();

        for(CandidateProfile candidate : candidates){
            try {
                candidateJobMatchingService.evaluateAndRank(candidate.getId());
            }catch (Exception e){
                System.out.println("Exception in running the evaluate and rank of this candidate" + candidate.getId());
            }
        }
    }
}
