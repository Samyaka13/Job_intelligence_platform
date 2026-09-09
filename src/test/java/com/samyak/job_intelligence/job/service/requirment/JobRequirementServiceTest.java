package com.samyak.job_intelligence.job.service.requirment;

import com.samyak.job_intelligence.job.domain.Job;
import com.samyak.job_intelligence.job.domain.JobRequirement;
import com.samyak.job_intelligence.job.domain.RequirementType;
import com.samyak.job_intelligence.job.repository.JobRequirementRepository;

import com.samyak.job_intelligence.job.service.JobService;
import com.samyak.job_intelligence.job.service.requirement.ExtractedJobRequirement;
import com.samyak.job_intelligence.job.service.requirement.JobRequirementService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JobRequirementServiceTest {

    private final JobRequirementRepository jobRequirementRepository =
            mock(JobRequirementRepository.class);

    private final JobService jobService =
            mock(JobService.class);

    private final JobRequirementService service =
            new JobRequirementService(
                    jobRequirementRepository,
                    jobService
            );

    @Test
    void shouldReplaceExistingRequirements() {

        // Existing requirement in database
        JobRequirement existingJava = mock(JobRequirement.class);

        // New requirements extracted from the job description
        ExtractedJobRequirement newJava =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Java",
                        "java",
                        true,
                        null,
                        "Java is required."
                );

        ExtractedJobRequirement newKafka =
                new ExtractedJobRequirement(
                        RequirementType.TECHNOLOGY,
                        "Kafka",
                        "kafka",
                        false,
                        null,
                        "Kafka is a plus."
                );

        Job job = mock(Job.class);

        when(jobService.getById(1L))
                .thenReturn(job);

        when(jobRequirementRepository.findByJobId(1L))
                .thenReturn(List.of(existingJava));

        when(jobRequirementRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<JobRequirement> result =
                service.replaceRequirements(
                        1L,
                        List.of(newJava, newKafka)
                );

        // Verify old requirements were deleted
        verify(jobRequirementRepository)
                .deleteAll(List.of(existingJava));

        // Verify two new requirements were saved
        verify(jobRequirementRepository)
                .saveAll(argThat(requirements ->
                        StreamSupport.stream(requirements.spliterator(), false)
                                .count() == 2
                ));
        // Verify returned result contains two requirements
        assertThat(result)
                .hasSize(2);

        // Verify the newly-created requirements contain the expected data
        assertThat(result.getFirst().getRequirementType())
                .isEqualTo(RequirementType.TECHNOLOGY);

        assertThat(result.get(0).getValue())
                .isEqualTo("Java");

        assertThat(result.get(0).isMandatory())
                .isTrue();

        assertThat(result.get(1).getRequirementType())
                .isEqualTo(RequirementType.TECHNOLOGY);

        assertThat(result.get(1).getValue())
                .isEqualTo("Kafka");

        assertThat(result.get(1).isMandatory())
                .isFalse();
    }

    @Test
    void shouldReturnEmptyWhenThereAreNoExtractedRequirements() {

        JobRequirement existingJava = mock(JobRequirement.class);

        Job job = mock(Job.class);

        when(jobService.getById(1L))
                .thenReturn(job);

        when(jobRequirementRepository.findByJobId(1L))
                .thenReturn(List.of(existingJava));

        List<JobRequirement> result =
                service.replaceRequirements(
                        1L,
                        List.of()
                );

        // Existing requirements must be deleted
        verify(jobRequirementRepository)
                .deleteAll(List.of(existingJava));

        // No new requirements should be saved
        verify(jobRequirementRepository, never())
                .saveAll(anyList());

        // Result should be empty
        assertThat(result)
                .isEmpty();
    }
}