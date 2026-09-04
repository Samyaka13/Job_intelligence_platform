package com.samyak.job_intelligence.job.domain;

import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;


import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "job_requirements",indexes = {
        @Index(name = "idx_job_requirements_job_id",columnList = "job_id"),
        @Index(name = "idx_job_requirements_type",columnList = "requirement_type"),
        @Index(name = "idx_job_requirements_normalized_value",columnList = "normalized_value"),
        @Index(name = "idx_job_requirements_mandatory",columnList = "is_mandatory")
})
public class JobRequirement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "job_id",nullable = false,foreignKey = @ForeignKey(name = "fk_job_requirements_job"))
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type",length = 50,nullable = false)
    private RequirementType requirementType;

    @Column(length = 500,nullable = false)
    private String value;

    @Column(name="normalized_value",length = 500,nullable = false)
    private String normalizedValue;

    @Column(name = "is_mandatory",nullable = false)
    private boolean mandatory;

    @Column(name ="years_required",precision = 4,scale = 1)
    private BigDecimal yearsRequired;

    @Column(name = "requirement_text")
    private String requirementText;

    protected JobRequirement(){
        //required by JPA
    }

    public JobRequirement(Job job, RequirementType requirementType, String value, String normalizedValue, boolean mandatory, BigDecimal yearsRequired, String requirementText) {
        this.job = job;
        this.requirementType = requirementType;
        this.value = value;
        this.normalizedValue = normalizedValue;
        this.mandatory = mandatory;
        this.yearsRequired = yearsRequired;
        this.requirementText = requirementText;
    }
}
