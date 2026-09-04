package com.samyak.job_intelligence.candidate.domain;

import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "candidate_profiles",indexes = {@Index(name="idx_candidate_profiles_active",columnList = "is_active")})
public class CandidateProfile extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false ,length = 255)
    private String name;

    @Column(nullable = false,length = 320,unique = true)
    private String email;

    @Column(name = "current_location")
    private String currentLocation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_locations",columnDefinition = "jsonb")
    private JsonNode preferredLocations;

    @Column(name = "minimum_salary" ,precision = 12,scale = 2)
    private BigDecimal minimumSalary;

    @Column(name = "minimum_salary_currency")
    private String minimumSalaryCurrency;

    @Column(name = "is_active" ,nullable = false)
    private boolean isActive;

    @Column(name = "experience_years",precision = 4,scale = 1)
    private BigDecimal experienceYears;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_employment_types", columnDefinition = "jsonb")
    private JsonNode preferredEmploymentTypes;

    protected CandidateProfile(){
        //required by JPA
    }

    public CandidateProfile(String name,String email,String currentLocation,JsonNode preferredLocations,BigDecimal minimumSalary,String minimumSalaryCurrency,boolean isActive,BigDecimal experienceYears,JsonNode preferredEmploymentTypes){
        this.name = name;
        this.email = email;
        this.currentLocation = currentLocation;
        this.preferredLocations = preferredLocations;
        this.minimumSalary = minimumSalary;
        this.minimumSalaryCurrency = minimumSalaryCurrency;
        this.isActive = isActive;
        this.experienceYears = experienceYears;
        this.preferredEmploymentTypes = preferredEmploymentTypes;
    }
}
