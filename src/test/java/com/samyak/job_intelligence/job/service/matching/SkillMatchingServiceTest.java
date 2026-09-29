package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementMatchMode;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SkillMatchingServiceTest {

    private final CandidateSkillRepository candidateSkillRepository =
            mock(CandidateSkillRepository.class);

    private final JobRequirementRepository jobRequirementRepository =
            mock(JobRequirementRepository.class);

    private final SkillMatchingService service =
            new SkillMatchingService(
                    candidateSkillRepository,
                    jobRequirementRepository
            );

    @Test
    void shouldMatchAllRequiredSkills() {

        JobRequirement java =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        true
                );

        JobRequirement spring =
                requirement(
                        "spring boot",
                        "spring-group",
                        RequirementMatchMode.SINGLE,
                        true
                );

        givenRequirements(List.of(java, spring));

        CandidateSkill candidateJava = candidateSkill("java");
        CandidateSkill candidateSpring = candidateSkill("spring boot");

        givenCandidateSkills(candidateJava, candidateSpring);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java", "spring boot"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());

        assertEquals(
                List.of("java", "spring boot"),
                result.mandatorySkills()
        );

        assertTrue(result.missingMandatorySkills().isEmpty());

        assertEquals(2, result.totalRequirements());
        assertEquals(2, result.mandatoryRequirements());
        assertEquals(2, result.matchedRequirements());
        assertEquals(2, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldDistinguishMissingOptionalSkills() {

        JobRequirement java =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        true
                );

        JobRequirement kafka =
                requirement(
                        "kafka",
                        "kafka-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        givenRequirements(List.of(java, kafka));

        CandidateSkill candidateJava = candidateSkill("java");

        givenCandidateSkills(candidateJava);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.matchedSkill()
        );

        assertEquals(
                List.of("kafka"),
                result.missingSkill()
        );

        assertEquals(
                List.of("java"),
                result.mandatorySkills()
        );

        assertTrue(result.missingMandatorySkills().isEmpty());

        assertEquals(2, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(1, result.matchedRequirements());
        assertEquals(1, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldIdentifyMissingMandatorySkills() {

        JobRequirement java =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        true
                );

        JobRequirement kafka =
                requirement(
                        "kafka",
                        "kafka-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        givenRequirements(List.of(java, kafka));

        givenCandidateSkills();

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("java", "kafka"),
                result.missingSkill()
        );

        assertEquals(
                List.of("java"),
                result.mandatorySkills()
        );

        assertEquals(
                List.of("java"),
                result.missingMandatorySkills()
        );

        assertEquals(2, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(0, result.matchedRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldTreatDuplicateRequirementRowsInSameGroupAsOneRequirement() {

        JobRequirement optionalJava =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        JobRequirement mandatoryJava =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        true
                );

        givenRequirements(List.of(optionalJava, mandatoryJava));

        givenCandidateSkills();

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("java"),
                result.missingSkill()
        );

        assertEquals(
                List.of("java"),
                result.mandatorySkills()
        );

        assertEquals(
                List.of("java"),
                result.missingMandatorySkills()
        );

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(0, result.matchedRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldReturnMissingSkills() {

        JobRequirement java =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        JobRequirement spring =
                requirement(
                        "spring boot",
                        "spring-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        JobRequirement kafka =
                requirement(
                        "kafka",
                        "kafka-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        givenRequirements(List.of(java, spring, kafka));

        CandidateSkill candidateJava = candidateSkill("java");
        CandidateSkill candidateSpring = candidateSkill("spring boot");

        givenCandidateSkills(candidateJava, candidateSpring);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java", "spring boot"),
                result.matchedSkill()
        );

        assertEquals(
                List.of("kafka"),
                result.missingSkill()
        );

        assertEquals(3, result.totalRequirements());
        assertEquals(0, result.mandatoryRequirements());
        assertEquals(2, result.matchedRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldReturnAllSkillsAsMissingWhenCandidateHasNoMatchingSkills() {

        JobRequirement java =
                requirement(
                        "java",
                        "java-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        JobRequirement kafka =
                requirement(
                        "kafka",
                        "kafka-group",
                        RequirementMatchMode.SINGLE,
                        false
                );

        givenRequirements(List.of(java, kafka));

        givenCandidateSkills();

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("java", "kafka"),
                result.missingSkill()
        );

        assertEquals(2, result.totalRequirements());
        assertEquals(0, result.matchedRequirements());
    }

    @Test
    void shouldReturnEmptyResultWhenJobHasNoSkillRequirements() {

        givenRequirements(List.of());

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());
        assertTrue(result.missingSkill().isEmpty());
        assertTrue(result.mandatorySkills().isEmpty());
        assertTrue(result.missingMandatorySkills().isEmpty());

        assertEquals(0, result.totalRequirements());
        assertEquals(0, result.mandatoryRequirements());
        assertEquals(0, result.matchedRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());

        verifyNoInteractions(candidateSkillRepository);
    }

    @Test
    void shouldMatchAnyOfRequirementWhenOneSkillIsPresent() {

        JobRequirement java =
                requirement(
                        "java",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        false
                );

        JobRequirement python =
                requirement(
                        "python",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        false
                );

        JobRequirement go =
                requirement(
                        "go",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        false
                );

        givenRequirements(List.of(java, python, go));

        CandidateSkill candidatePython = candidateSkill("python");

        givenCandidateSkills(candidatePython);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("python"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.matchedRequirements());
        assertEquals(0, result.mandatoryRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldFailAnyOfRequirementWhenNoSkillIsPresent() {

        JobRequirement java =
                requirement(
                        "java",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        false
                );

        JobRequirement python =
                requirement(
                        "python",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        false
                );

        JobRequirement go =
                requirement(
                        "go",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        false
                );

        givenRequirements(List.of(java, python, go));

        givenCandidateSkills();

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("java", "python", "go"),
                result.missingSkill()
        );

        assertEquals(1, result.totalRequirements());
        assertEquals(0, result.matchedRequirements());
    }

    @Test
    void shouldMatchAllOfRequirementWhenAllSkillsArePresent() {

        JobRequirement java =
                requirement(
                        "java",
                        "backend-stack",
                        RequirementMatchMode.ALL_OF,
                        false
                );

        JobRequirement spring =
                requirement(
                        "spring boot",
                        "backend-stack",
                        RequirementMatchMode.ALL_OF,
                        false
                );

        givenRequirements(List.of(java, spring));

        CandidateSkill candidateJava = candidateSkill("java");
        CandidateSkill candidateSpring = candidateSkill("spring boot");

        givenCandidateSkills(candidateJava, candidateSpring);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java", "spring boot"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.matchedRequirements());
    }

    @Test
    void shouldFailAllOfRequirementWhenOneSkillIsMissing() {

        JobRequirement java =
                requirement(
                        "java",
                        "backend-stack",
                        RequirementMatchMode.ALL_OF,
                        false
                );

        JobRequirement spring =
                requirement(
                        "spring boot",
                        "backend-stack",
                        RequirementMatchMode.ALL_OF,
                        false
                );

        givenRequirements(List.of(java, spring));

        CandidateSkill candidateJava = candidateSkill("java");

        givenCandidateSkills(candidateJava);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.matchedSkill()
        );

        assertEquals(
                List.of("spring boot"),
                result.missingSkill()
        );

        assertEquals(1, result.totalRequirements());
        assertEquals(0, result.matchedRequirements());
    }

    @Test
    void shouldMatchMandatoryAnyOfWhenOneAlternativeIsPresent() {

        JobRequirement java =
                requirement(
                        "java",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        true
                );

        JobRequirement python =
                requirement(
                        "python",
                        "backend-language",
                        RequirementMatchMode.ANY_OF,
                        true
                );

        givenRequirements(List.of(java, python));

        CandidateSkill candidateJava = candidateSkill("java");

        givenCandidateSkills(candidateJava);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(1, result.matchedRequirements());
        assertEquals(1, result.matchedMandatoryRequirements());

        assertTrue(result.missingMandatorySkills().isEmpty());
    }

    @Test
    void shouldFailMandatoryAllOfWhenOneSkillIsMissing() {

        JobRequirement java =
                requirement(
                        "java",
                        "backend-stack",
                        RequirementMatchMode.ALL_OF,
                        true
                );

        JobRequirement spring =
                requirement(
                        "spring boot",
                        "backend-stack",
                        RequirementMatchMode.ALL_OF,
                        true
                );

        givenRequirements(List.of(java, spring));

        CandidateSkill candidateJava = candidateSkill("java");

        givenCandidateSkills(candidateJava);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.matchedSkill()
        );

        assertEquals(
                List.of("spring boot"),
                result.missingSkill()
        );

        assertEquals(
                List.of("spring boot"),
                result.missingMandatorySkills()
        );

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(0, result.matchedRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldNotMatchSkillWhenCandidateHasInsufficientExperience() {

        JobRequirement sql =
                requirement(
                        "sql",
                        "sql-group",
                        RequirementMatchMode.SINGLE,
                        true,
                        BigDecimal.valueOf(5)
                );

        givenRequirements(List.of(sql));

        CandidateSkill candidateSql =
                candidateSkill(
                        "sql",
                        BigDecimal.valueOf(1)
                );

        givenCandidateSkills(candidateSql);

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("sql"),
                result.missingSkill()
        );

        assertEquals(
                List.of("sql"),
                result.mandatorySkills()
        );

        assertEquals(
                List.of("sql"),
                result.missingMandatorySkills()
        );

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(0, result.matchedRequirements());
        assertEquals(0, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldMatchSkillWhenCandidateHasExactlyRequiredExperience() {

        JobRequirement sql =
                requirement(
                        "sql",
                        "sql-group",
                        RequirementMatchMode.SINGLE,
                        true,
                        BigDecimal.valueOf(5)
                );

        givenRequirements(List.of(sql));

        CandidateSkill candidateSql =
                candidateSkill(
                        "sql",
                        BigDecimal.valueOf(5)
                );

        givenCandidateSkills(candidateSql);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("sql"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());
        assertTrue(result.missingMandatorySkills().isEmpty());

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(1, result.matchedRequirements());
        assertEquals(1, result.matchedMandatoryRequirements());
    }

    @Test
    void shouldMatchSkillWhenCandidateHasMoreExperienceThanRequired() {

        JobRequirement sql =
                requirement(
                        "sql",
                        "sql-group",
                        RequirementMatchMode.SINGLE,
                        true,
                        BigDecimal.valueOf(5)
                );

        givenRequirements(List.of(sql));

        CandidateSkill candidateSql =
                candidateSkill(
                        "sql",
                        BigDecimal.valueOf(6)
                );

        givenCandidateSkills(candidateSql);

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("sql"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());
        assertTrue(result.missingMandatorySkills().isEmpty());

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
        assertEquals(1, result.matchedRequirements());
        assertEquals(1, result.matchedMandatoryRequirements());
    }

    private JobRequirement requirement(
            String normalizedValue,
            String groupId,
            RequirementMatchMode matchMode,
            boolean mandatory
    ) {
        JobRequirement requirement = mock(JobRequirement.class);

        when(requirement.getNormalizedValue())
                .thenReturn(normalizedValue);

        when(requirement.getGroupId())
                .thenReturn(groupId);

        when(requirement.getRequirementMatchMode())
                .thenReturn(matchMode);

        when(requirement.getRequirementType())
                .thenReturn(RequirementType.TECHNOLOGY);

        when(requirement.isMandatory())
                .thenReturn(mandatory);

        return requirement;
    }

    private JobRequirement requirement(
            String normalizedValue,
            String groupId,
            RequirementMatchMode matchMode,
            boolean mandatory,
            BigDecimal yearsRequired
    ) {
        JobRequirement requirement =
                requirement(
                        normalizedValue,
                        groupId,
                        matchMode,
                        mandatory
                );

        when(requirement.getYearsRequired())
                .thenReturn(yearsRequired);

        return requirement;
    }

    private CandidateSkill candidateSkill(String normalizedSkill) {
        CandidateSkill candidateSkill = mock(CandidateSkill.class);

        when(candidateSkill.getNormalizedSkill())
                .thenReturn(normalizedSkill);

        return candidateSkill;
    }

    private CandidateSkill candidateSkill(
            String normalizedSkill,
            BigDecimal yearsExperience
    ) {
        CandidateSkill candidateSkill =
                candidateSkill(normalizedSkill);

        when(candidateSkill.getYearsExperience())
                .thenReturn(yearsExperience);

        return candidateSkill;
    }

    private void givenRequirements(List<JobRequirement> requirements) {

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.TECHNOLOGY
        )).thenReturn(requirements);

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.LANGUAGE
        )).thenReturn(List.of());
    }

    private void givenCandidateSkills(CandidateSkill... candidateSkills) {

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        eq(1L),
                        anyList()
                ))
                .thenReturn(List.of(candidateSkills));
    }
}