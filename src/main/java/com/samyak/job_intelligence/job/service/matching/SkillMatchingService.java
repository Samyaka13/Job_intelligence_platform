package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

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
            return new SkillMatchResult(List.of(),List.of(),List.of(),0,0);
        }

        List<SkillRequirement> skillRequirements = requirements.stream()
                .collect(Collectors.toMap(JobRequirement::getNormalizedValue,
                        requirement -> new SkillRequirement(requirement.getNormalizedValue(),requirement.isMandatory()
                ),
                        (first,second) -> new SkillRequirement(first.skill(), first.mandatory() || second.mandatory()),
                        LinkedHashMap::new
                        ))
                .values().stream().toList();

        List<String> requiredSkills = skillRequirements.stream().map(SkillRequirement::skill).toList();

        List<CandidateSkill> candidateSkills = candidateSkillRepository.findByCandidateProfile_IdAndNormalizedSkillIn(candidateProfileId,requiredSkills);

        Set<String> matchedSkillSet = candidateSkills.stream().map(CandidateSkill :: getNormalizedSkill).collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> matchedSkills = requiredSkills.stream().filter(matchedSkillSet::contains).toList();

        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !matchedSkillSet.contains(skill))
                .toList();

        List<String> missingMandatorySkills = skillRequirements.stream()
                .filter(requirement -> requirement.mandatory()  && !matchedSkillSet.contains(requirement.skill())
                ).map(SkillRequirement::skill)
                .toList();

        long mandatoryRequirements = skillRequirements.stream()
                .filter(SkillRequirement::mandatory)
                .count();

        return new SkillMatchResult(
                matchedSkills,
                missingSkills,
                missingMandatorySkills,
                requiredSkills.size(),
                (int) mandatoryRequirements
        );
    }
}
