package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SkillMatchingService {
    private final CandidateSkillRepository candidateSkillRepository;
    private final JobRequirementRepository jobRequirementRepository;

    public SkillMatchingService(CandidateSkillRepository candidateSkillRepository, JobRequirementRepository jobRequirementRepository) {
        this.candidateSkillRepository = candidateSkillRepository;
        this.jobRequirementRepository = jobRequirementRepository;
    }

    public SkillMatchResult match(Long candidateProfileId, Long jobId){
        List<JobRequirement> requirements = new ArrayList<>();

        requirements.addAll(jobRequirementRepository.findByJobIdAndRequirementType(jobId, RequirementType.LANGUAGE));

        requirements.addAll(jobRequirementRepository.findByJobIdAndRequirementType(jobId,RequirementType.TECHNOLOGY));

        if(requirements.isEmpty()){
            return new SkillMatchResult(List.of(),List.of(),0);
        }

        List<String> requiredSkills = requirements.stream().map(JobRequirement :: getNormalizedValue).distinct().toList();

        List<CandidateSkill> candidateSkills = candidateSkillRepository.findByCandidateProfile_IdAndNormalizedSkillIn(candidateProfileId,requiredSkills);

        Set<String> matchedSkillSet = candidateSkills.stream().map(CandidateSkill :: getNormalizedSkill).collect(java.util.stream.Collectors.toSet());

        List<String> matchedSkills = requiredSkills.stream().filter(matchedSkillSet::contains).toList();

        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !matchedSkillSet.contains(skill))
                .toList();

        return new SkillMatchResult(
                matchedSkills,
                missingSkills,
                requiredSkills.size()
        );
    }
}
