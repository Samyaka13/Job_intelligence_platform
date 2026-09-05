package com.samyak.job_intelligence.job.service.matching;

import com.samyak.job_intelligence.candidate.domain.CandidateSkill;
import com.samyak.job_intelligence.candidate.repository.CandidateSkillRepository;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        JobRequirement java = mock(JobRequirement.class);
        when(java.getNormalizedValue()).thenReturn("java");
        when(java.isMandatory()).thenReturn(true);

        JobRequirement spring = mock(JobRequirement.class);
        when(spring.getNormalizedValue()).thenReturn("spring boot");
        when(spring.isMandatory()).thenReturn(true);

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.TECHNOLOGY
        )).thenReturn(List.of(java, spring));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.LANGUAGE
        )).thenReturn(List.of());

        CandidateSkill candidateJava = mock(CandidateSkill.class);
        when(candidateJava.getNormalizedSkill()).thenReturn("java");

        CandidateSkill candidateSpring = mock(CandidateSkill.class);
        when(candidateSpring.getNormalizedSkill()).thenReturn("spring boot");

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        eq(1L),
                        anyList()
                ))
                .thenReturn(List.of(candidateJava, candidateSpring));

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java", "spring boot"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());
        assertTrue(result.missingMandatorySkills().isEmpty());
        assertEquals(2, result.totalRequirements());
        assertEquals(2, result.mandatoryRequirements());
    }



    @Test
    void shouldDistinguishMissingOptionalSkills() {

        JobRequirement java = mock(JobRequirement.class);
        when(java.getNormalizedValue()).thenReturn("java");
        when(java.isMandatory()).thenReturn(true);

        JobRequirement kafka = mock(JobRequirement.class);
        when(kafka.getNormalizedValue()).thenReturn("kafka");
        when(kafka.isMandatory()).thenReturn(false);

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.TECHNOLOGY
        )).thenReturn(List.of(java, kafka));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.LANGUAGE
        )).thenReturn(List.of());

        CandidateSkill candidateJava = mock(CandidateSkill.class);
        when(candidateJava.getNormalizedSkill()).thenReturn("java");

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        1L,
                        List.of("java", "kafka")
                ))
                .thenReturn(List.of(candidateJava));

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.matchedSkill()
        );

        assertEquals(
                List.of("kafka"),
                result.missingSkill()
        );

        assertTrue(result.missingMandatorySkills().isEmpty());

        assertEquals(2, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
    }

    @Test
    void shouldIdentifyMissingMandatorySkills() {

        JobRequirement java = mock(JobRequirement.class);
        when(java.getNormalizedValue()).thenReturn("java");
        when(java.isMandatory()).thenReturn(true);

        JobRequirement kafka = mock(JobRequirement.class);
        when(kafka.getNormalizedValue()).thenReturn("kafka");
        when(kafka.isMandatory()).thenReturn(false);

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.TECHNOLOGY
        )).thenReturn(List.of(java, kafka));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.LANGUAGE
        )).thenReturn(List.of());

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        1L,
                        List.of("java", "kafka")
                ))
                .thenReturn(List.of());

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("java", "kafka"),
                result.missingSkill()
        );

        assertEquals(
                List.of("java"),
                result.missingMandatorySkills()
        );

        assertEquals(2, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
    }

    @Test
    void shouldTreatDuplicateSkillAsMandatoryWhenAnyOccurrenceIsMandatory() {

        JobRequirement optionalJava = mock(JobRequirement.class);
        when(optionalJava.getNormalizedValue()).thenReturn("java");
        when(optionalJava.isMandatory()).thenReturn(false);

        JobRequirement mandatoryJava = mock(JobRequirement.class);
        when(mandatoryJava.getNormalizedValue()).thenReturn("java");
        when(mandatoryJava.isMandatory()).thenReturn(true);

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.TECHNOLOGY
        )).thenReturn(List.of(optionalJava, mandatoryJava));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L, RequirementType.LANGUAGE
        )).thenReturn(List.of());

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        1L,
                        List.of("java")
                ))
                .thenReturn(List.of());

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.missingSkill()
        );

        assertEquals(
                List.of("java"),
                result.missingMandatorySkills()
        );

        assertEquals(1, result.totalRequirements());
        assertEquals(1, result.mandatoryRequirements());
    }

    @Test
    void shouldReturnMissingSkills() {

        JobRequirement java = mock(JobRequirement.class);
        when(java.getNormalizedValue()).thenReturn("java");


        JobRequirement spring = mock(JobRequirement.class);
        when(spring.getNormalizedValue()).thenReturn("spring boot");

        JobRequirement kafka = mock(JobRequirement.class);
        when(kafka.getNormalizedValue()).thenReturn("kafka");

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.TECHNOLOGY
        )).thenReturn(List.of(java, spring, kafka));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.LANGUAGE
        )).thenReturn(List.of());

        CandidateSkill candidateJava = mock(CandidateSkill.class);
        when(candidateJava.getNormalizedSkill()).thenReturn("java");

        CandidateSkill candidateSpring = mock(CandidateSkill.class);
        when(candidateSpring.getNormalizedSkill()).thenReturn("spring boot");

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        eq(1L),
                        anyList()
                ))
                .thenReturn(List.of(candidateJava, candidateSpring));

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
    }

    @Test
    void shouldReturnAllSkillsAsMissingWhenCandidateHasNoMatchingSkills() {

        JobRequirement java = mock(JobRequirement.class);
        when(java.getNormalizedValue()).thenReturn("java");

        JobRequirement kafka = mock(JobRequirement.class);
        when(kafka.getNormalizedValue()).thenReturn("kafka");

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.TECHNOLOGY
        )).thenReturn(List.of(java, kafka));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.LANGUAGE
        )).thenReturn(List.of());

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        1L,
                        List.of("java", "kafka")
                ))
                .thenReturn(List.of());

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());

        assertEquals(
                List.of("java", "kafka"),
                result.missingSkill()
        );

        assertEquals(2, result.totalRequirements());
    }

    @Test
    void shouldReturnEmptyResultWhenJobHasNoSkillRequirements() {

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.TECHNOLOGY
        )).thenReturn(List.of());

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.LANGUAGE
        )).thenReturn(List.of());

        SkillMatchResult result = service.match(1L, 1L);

        assertTrue(result.matchedSkill().isEmpty());
        assertTrue(result.missingSkill().isEmpty());
        assertEquals(0, result.totalRequirements());

        verifyNoInteractions(candidateSkillRepository);
    }

    @Test
    void shouldTreatDuplicateRequirementsAsOneSkill() {

        JobRequirement java1 = mock(JobRequirement.class);
        when(java1.getNormalizedValue()).thenReturn("java");

        JobRequirement java2 = mock(JobRequirement.class);
        when(java2.getNormalizedValue()).thenReturn("java");

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.TECHNOLOGY
        )).thenReturn(List.of(java1, java2));

        when(jobRequirementRepository.findByJobIdAndRequirementType(
                1L,
                RequirementType.LANGUAGE
        )).thenReturn(List.of());

        CandidateSkill candidateJava = mock(CandidateSkill.class);
        when(candidateJava.getNormalizedSkill()).thenReturn("java");

        when(candidateSkillRepository
                .findByCandidateProfile_IdAndNormalizedSkillIn(
                        1L,
                        List.of("java")
                ))
                .thenReturn(List.of(candidateJava));

        SkillMatchResult result = service.match(1L, 1L);

        assertEquals(
                List.of("java"),
                result.matchedSkill()
        );

        assertTrue(result.missingSkill().isEmpty());

        assertEquals(1, result.totalRequirements());
    }

}