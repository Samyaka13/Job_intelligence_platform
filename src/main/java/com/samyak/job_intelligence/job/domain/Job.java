package com.samyak.job_intelligence.job.domain;

import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import com.samyak.job_intelligence.company.domain.Company;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_company_id",columnList = "company_id"),
                @Index(name = "idx_jobs_posted_at",columnList = "posted_at"),
                @Index(name = "idx_jobs_status",columnList = "status"),
                @Index(name = "idx_jobs_last_seen_at",columnList = "last_seen_at"),

        }
)
public class Job extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =FetchType.LAZY,optional = false)
    @JoinColumn(name = "company_id" ,nullable = false,foreignKey = @ForeignKey(name = "fk_jobs_company"))
    private Company company;

    @Column(nullable = false,length = 500)
    private String title;

    @Column(name = "normalized_title",nullable = false,length = 500)
    private String normalizedTitle;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String description;

    @Column(name = "employment_type",nullable = false,length = 50)
    private String employmentType;

    @Column(name = "seniority_level",nullable = false,length = 50)
    private String seniorityLevel;

    @Column(name = "experience_min_years" ,precision = 4,scale = 1)
    private BigDecimal experienceMinYears;

    @Column(name = "experience_max_years", precision = 4,scale = 1)
    private BigDecimal experienceMaxYears;

    @Column(name = "salary_min",precision = 12,scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max",precision = 12,scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency",length = 3)
    private String salaryCurrency;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "canonical_application_url",length = 2048)
    private String canonicalApplicationUrl;

    @Column(name = "canonical_fingerprint",nullable = false,length = 64,unique = true)
    private String canonicalFingerprint;

    @Column(name = "description_hash",length = 64)
    private String descriptionHash;

    @Column(length = 30,nullable = false)
    private String status = "ACTIVE";

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    protected Job(){
        //requried by JPA
    }

    public Job(
            Company company,
            String title,
            String normalizedTitle,
            String description,
            String employmentType,
            String seniorityLevel,
            BigDecimal experienceMinYears,
            BigDecimal experienceMaxYears,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String salaryCurrency,
            Instant postedAt,
            Instant expiresAt,
            String canonicalApplicationUrl,
            String canonicalFingerprint,
            String descriptionHash
    ) {
        this.company = company;
        this.title = title;
        this.normalizedTitle = normalizedTitle;
        this.description = description;
        this.employmentType = employmentType;
        this.seniorityLevel = seniorityLevel;
        this.experienceMinYears = experienceMinYears;
        this.experienceMaxYears = experienceMaxYears;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.postedAt = postedAt;
        this.expiresAt = expiresAt;
        this.canonicalApplicationUrl = canonicalApplicationUrl;
        this.canonicalFingerprint = canonicalFingerprint;
        this.descriptionHash = descriptionHash;

        Instant now = Instant.now();
        this.firstSeenAt = now;
        this.lastSeenAt = now;
    }

}
