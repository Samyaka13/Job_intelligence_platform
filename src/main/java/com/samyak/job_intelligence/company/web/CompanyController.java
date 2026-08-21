package com.samyak.job_intelligence.company.web;

import com.samyak.job_intelligence.company.domain.Company;
import com.samyak.job_intelligence.company.service.CompanyService;
import com.samyak.job_intelligence.company.web.dto.CompanyCreateRequest;
import com.samyak.job_intelligence.company.web.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(
            @Valid @RequestBody CompanyCreateRequest request
    ){
        Company company = companyService.create(request.canonicalName(),request.displayName(),request.websiteUrl());
        return CompanyResponse.from(company);
    }

    @GetMapping("/{id}")
    public CompanyResponse getById(@PathVariable Long id){
        return CompanyResponse.from(companyService.getById(id));
    }


}
