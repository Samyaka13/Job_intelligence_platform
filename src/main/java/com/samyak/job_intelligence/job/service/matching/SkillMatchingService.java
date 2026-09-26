package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementMatchMode;
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
            return new SkillMatchResult(List.of(),List.of(),List.of(),List.of(),0,0,0,0);
        }

        Map<String, RequirementGroup> groups = requirements.stream()
                .collect(Collectors.groupingBy(
                        this::effectiveGroupId,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                RequirementGroup::from
                        )
                ));

        List<String> requiredSkills = groups.values().stream()
                .flatMap(group -> group.skills().stream())
                .distinct()
                .toList();

        List<CandidateSkill> candidateSkills =
                candidateSkillRepository
                        .findByCandidateProfile_IdAndNormalizedSkillIn(
                                candidateProfileId,
                                requiredSkills
                        );

        Set<String> candidateSkillSet = candidateSkills.stream()
                .map(CandidateSkill::getNormalizedSkill)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> mandatorySkills = new ArrayList<>();
        List<String> missingMandatorySkills = new ArrayList<>();

        int matchedRequirements = 0;
        int mandatoryRequirements = 0;
        int matchedMandatoryRequirements = 0;

        for (RequirementGroup group : groups.values()) {

            boolean matched = isGroupMatched(group, candidateSkillSet);

            List<String> groupMatchedSkills = group.skills().stream()
                    .filter(candidateSkillSet::contains)
                    .toList();

            List<String> groupMissingSkills = group.skills().stream()
                    .filter(skill -> !candidateSkillSet.contains(skill))
                    .toList();

            if (matched) {
                matchedRequirements++;
                matchedSkills.addAll(groupMatchedSkills);
            } else {
                missingSkills.addAll(groupMissingSkills);
            }

            if (group.mandatory()) {
                mandatoryRequirements++;
                mandatorySkills.addAll(group.skills());

                if (matched) {
                    matchedMandatoryRequirements++;
                } else {
                    missingMandatorySkills.addAll(groupMissingSkills);
                }
            }
        }

        return new SkillMatchResult(
                List.copyOf(matchedSkills),
                List.copyOf(missingSkills),
                List.copyOf(mandatorySkills),
                List.copyOf(missingMandatorySkills),
                groups.size(),
                mandatoryRequirements,
                matchedRequirements,
                matchedMandatoryRequirements
        );
    }

    private boolean isGroupMatched(
            RequirementGroup group,
            Set<String> candidateSkillSet
    ) {
        return switch (group.matchMode()) {

            case SINGLE, ANY_OF ->
                    group.skills().stream()
                            .anyMatch(candidateSkillSet::contains);

            case ALL_OF ->
                    candidateSkillSet.containsAll(group.skills());
        };
    }

    private String effectiveGroupId(JobRequirement requirement) {

        if (requirement.getGroupId() != null
                && !requirement.getGroupId().isBlank()) {

            return requirement.getGroupId();
        }

        /*
         * Backward compatibility for requirements created before
         * groupId was introduced.
         */
        return "legacy:"
                + requirement.getRequirementType()
                + ":"
                + requirement.getNormalizedValue();
    }

    private record RequirementGroup(
            String groupId,
            RequirementMatchMode matchMode,
            boolean mandatory,
            List<String> skills
    ) {

        static RequirementGroup from(List<JobRequirement> requirements) {

            if (requirements.isEmpty()) {
                throw new IllegalArgumentException(
                        "Requirement group cannot be empty"
                );
            }

            RequirementMatchMode matchMode = requirements.getFirst()
                    .getRequirementMatchMode();

            if (matchMode == null) {
                matchMode = RequirementMatchMode.SINGLE;
            }

            boolean mandatory = requirements.stream()
                    .anyMatch(JobRequirement::isMandatory);

            List<String> skills = requirements.stream()
                    .map(JobRequirement::getNormalizedValue)
                    .filter(Objects::nonNull)
                    .filter(skill -> !skill.isBlank())
                    .distinct()
                    .toList();

            return new RequirementGroup(
                    requirements.getFirst().getGroupId(),
                    matchMode,
                    mandatory,
                    skills
            );
        }
    }
}