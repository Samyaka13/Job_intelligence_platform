package com.samyak.job_intelligence.source.domain;

import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import com.samyak.job_intelligence.company.domain.Company;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;

import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "source_configurations",
        indexes = {
                @Index(name = "idx_source_configurations_company_id",columnList = "company_id"),
                @Index(name = "idx_source_configurations_job_source_id",columnList = "job_source_id"),
                @Index(name = "idx_source_configurations_enabled",columnList = "enabled")
        }
)
@Getter
public class SourceConfiguration extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "company_id",nullable = false,foreignKey = @ForeignKey(name = "fk_source_configurations_company"))
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "job_source_id",nullable = false,foreignKey = @ForeignKey(name = "fk_source_configurations_job_source"))
    private JobSource jobSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false,columnDefinition = "jsonb NOT NULL DEFAULT '{}'::jsonb")
    private JsonNode configuration;

    @Column(nullable = false,columnDefinition = "boolean NOT NULL DEFAULT true")
    private boolean enabled;


   protected SourceConfiguration() {
       //required by JPA
    }

    public SourceConfiguration(Company company, JobSource jobSource, JsonNode configuration, boolean enabled) {
        this.company = company;
        this.jobSource = jobSource;
        this.configuration = configuration;
        this.enabled = enabled;
    }
}
