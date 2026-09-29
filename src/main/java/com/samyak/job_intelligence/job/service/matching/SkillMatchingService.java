package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementMatchMode;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillMatchingService {

    private final CandidateSkillRepository candidateSkillRepository;
    private final JobRequirementRepository jobRequirementRepository;

    public SkillMatchingService(
            CandidateSkillRepository candidateSkillRepository,
            JobRequirementRepository jobRequirementRepository
    ) {
        this.candidateSkillRepository = candidateSkillRepository;
        this.jobRequirementRepository = jobRequirementRepository;
    }

    public SkillMatchResult match(Long candidateProfileId, Long jobId) {

        List<JobRequirement> requirements = new ArrayList<>();

        requirements.addAll(
                jobRequirementRepository.findByJobIdAndRequirementType(
                        jobId,
                        RequirementType.LANGUAGE
                )
        );

        requirements.addAll(
                jobRequirementRepository.findByJobIdAndRequirementType(
                        jobId,
                        RequirementType.TECHNOLOGY
                )
        );

        if (requirements.isEmpty()) {
            return new SkillMatchResult(
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    0,
                    0,
                    0,
                    0
            );
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
                .flatMap(group ->
                        group.requirements().stream()
                                .map(RequirementItem::skill)
                )
                .distinct()
                .toList();

        List<CandidateSkill> candidateSkills =
                candidateSkillRepository
                        .findByCandidateProfile_IdAndNormalizedSkillIn(
                                candidateProfileId,
                                requiredSkills
                        );

        Map<String, CandidateSkill> candidateSkillMap =
                candidateSkills.stream()
                        .filter(skill ->
                                skill.getNormalizedSkill() != null
                        )
                        .collect(Collectors.toMap(
                                CandidateSkill::getNormalizedSkill,
                                skill -> skill,
                                (first, second) -> first
                        ));

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> mandatorySkills = new ArrayList<>();
        List<String> missingMandatorySkills = new ArrayList<>();

        int matchedRequirements = 0;
        int mandatoryRequirements = 0;
        int matchedMandatoryRequirements = 0;

        for (RequirementGroup group : groups.values()) {

            Set<String> matchedGroupSkills = group.requirements().stream()
                    .filter(requirement ->
                            satisfiesRequirement(
                                    requirement,
                                    candidateSkillMap
                            )
                    )
                    .map(RequirementItem::skill)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Set<String> missingGroupSkills = group.requirements().stream()
                    .filter(requirement ->
                            !satisfiesRequirement(
                                    requirement,
                                    candidateSkillMap
                            )
                    )
                    .map(RequirementItem::skill)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            boolean groupMatched =
                    isGroupMatched(
                            group,
                            candidateSkillMap
                    );

            if (groupMatched) {
                matchedRequirements++;
            }

            matchedSkills.addAll(matchedGroupSkills);

            if (!groupMatched) {
                missingSkills.addAll(missingGroupSkills);
            }

            if (group.mandatory()) {
                mandatoryRequirements++;
                mandatorySkills.addAll(
                        group.requirements().stream()
                                .map(RequirementItem::skill)
                                .distinct()
                                .toList()
                );

                if (!groupMatched) {
                    missingMandatorySkills.addAll(missingGroupSkills);
                } else {
                    matchedMandatoryRequirements++;
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
            Map<String, CandidateSkill> candidateSkillMap
    ) {

        return switch (group.matchMode()) {

            case SINGLE, ANY_OF ->
                    group.requirements().stream()
                            .anyMatch(requirement ->
                                    satisfiesRequirement(
                                            requirement,
                                            candidateSkillMap
                                    )
                            );

            case ALL_OF ->
                    group.requirements().stream()
                            .allMatch(requirement ->
                                    satisfiesRequirement(
                                            requirement,
                                            candidateSkillMap
                                    )
                            );
        };
    }

    private boolean satisfiesRequirement(
            RequirementItem requirement,
            Map<String, CandidateSkill> candidateSkillMap
    ) {

        CandidateSkill candidateSkill =
                candidateSkillMap.get(requirement.skill());

        if (candidateSkill == null) {
            return false;
        }

        BigDecimal yearsRequired =
                requirement.yearsRequired();

        if (yearsRequired == null) {
            return true;
        }

        BigDecimal candidateYears =
                candidateSkill.getYearsExperience();

        if (candidateYears == null) {
            return false;
        }

        return candidateYears.compareTo(yearsRequired) >= 0;
    }

    private String effectiveGroupId(JobRequirement requirement) {

        if (requirement.getGroupId() != null
                && !requirement.getGroupId().isBlank()) {

            return requirement.getGroupId();
        }

        return "legacy:"
                + requirement.getRequirementType()
                + ":"
                + requirement.getNormalizedValue();
    }

    private record RequirementItem(
            String skill,
            BigDecimal yearsRequired
    ) {
    }

    private record RequirementGroup(
            String groupId,
            RequirementMatchMode matchMode,
            boolean mandatory,
            List<RequirementItem> requirements
    ) {

        static RequirementGroup from(
                List<JobRequirement> requirements
        ) {

            if (requirements.isEmpty()) {
                throw new IllegalArgumentException(
                        "Requirement group cannot be empty"
                );
            }

            RequirementMatchMode matchMode =
                    requirements.getFirst().getRequirementMatchMode();

            if (matchMode == null) {
                matchMode = RequirementMatchMode.SINGLE;
            }

            boolean mandatory =
                    requirements.stream()
                            .anyMatch(JobRequirement::isMandatory);

            List<RequirementItem> items =
                    requirements.stream()
                            .map(requirement ->
                                    new RequirementItem(
                                            requirement.getNormalizedValue(),
                                            requirement.getYearsRequired()
                                    )
                            )
                            .filter(item ->
                                    item.skill() != null
                                            && !item.skill().isBlank()
                            )
                            .toList();

            return new RequirementGroup(
                    requirements.getFirst().getGroupId(),
                    matchMode,
                    mandatory,
                    items
            );
        }
    }
}