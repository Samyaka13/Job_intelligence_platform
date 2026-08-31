package com.samyak.job_intelligence.source.service;

import com.samyak.job_intelligence.source.domain.JobSource;
import com.samyak.job_intelligence.source.domain.JobSourceType;
import com.samyak.job_intelligence.source.repository.JobSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class JobSourceServiceTest {

    @Mock
    private JobSourceRepository jobSourceRepository;

    private JobSourceService jobSourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jobSourceService = new JobSourceService(jobSourceRepository);
    }

    @Test
    void shouldFindSourceByNormalizedCode() {

        JobSource source = new JobSource(
                "GREENHOUSE",
                "Greenhouse",
                JobSourceType.ATS,
                "https://boards.greenhouse.io"
        );

        when(jobSourceRepository.findByCode("GREENHOUSE"))
                .thenReturn(Optional.of(source));

        JobSource result =
                jobSourceService.getByCode(" greenhouse ");

        assertThat(result).isSameAs(source);
    }

    @Test
    void shouldThrowWhenSourceDoesNotExist() {

        when(jobSourceRepository.findByCode("GREENHOUSE"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                jobSourceService.getByCode(" greenhouse ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job source not found: GREENHOUSE");
    }
}