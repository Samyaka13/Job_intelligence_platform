package com.samyak.job_intelligence.source.web;

import com.samyak.job_intelligence.source.domain.SourceConfiguration;
import com.samyak.job_intelligence.source.service.SourceConfigurationService;
import com.samyak.job_intelligence.source.web.dto.SourceConfigurationCreateRequest;
import com.samyak.job_intelligence.source.web.dto.SourceConfigurationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/source-configurations")
public class SourceConfigurationController {
    private final SourceConfigurationService configurationService;

    public SourceConfigurationController(SourceConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SourceConfigurationResponse create(@PathVariable Long companyId, @Valid @RequestBody SourceConfigurationCreateRequest request){
        SourceConfiguration configuration = configurationService.create(companyId,request.sourceCode(),request.configuration());

        return SourceConfigurationResponse.from(configuration);
    }
}
