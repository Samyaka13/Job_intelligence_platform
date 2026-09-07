package com.samyak.job_intelligence.job.domain;


import com.samyak.job_intelligence.candidate.domain.CandidateProfile;
import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;


@Getter
@Entity
@Table(name = "job_matches",indexes = {@Index(name = "idx_job_matches_job_id",columnList = "job_id"),
        @Index(name = "idx_job_matches_candidate_id",columnList = "candidate_profile_id"),
        @Index(name = "idx_job_matches_final_score",columnList = "final_score"),
        @Index(name =  "idx_job_matches_evaluated_at",columnList = "evaluated_at"),
        @Index(name = "idx_job_matches_qualified",columnList = "hard_qualified")

})
public class JobMatch extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "job_id",nullable = false,foreignKey = @ForeignKey(name = "fk_job_matches_job"))
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "candidate_profile_id",nullable = false,foreignKey = @ForeignKey(name = "fk_job_matches_candidate"))
    private CandidateProfile candidateProfile;

    @Column(name = "hard_qualified" ,nullable = false)
    private boolean hardQualified;

    @Column(name = "skill_score" ,precision = 5 ,scale = 2)
    private BigDecimal skillScore;

    @Column(name = "experience_score",precision = 5 ,scale = 2)
    private BigDecimal experienceScore;

    @Column(name = "role_score",precision = 5,scale = 2)
    private BigDecimal roleScore;

    @Column(name = "location_score",precision = 5,scale = 2)
    private BigDecimal locationScore;

    @Column(name = "salary_score",precision = 5,scale = 2)
    private BigDecimal salaryScore;

    @Column(name = "semantic_score",precision = 5,scale = 2)
    private BigDecimal semanticScore;

    @Column(name = "final_score",precision = 5,scale = 2,nullable = false)
    private BigDecimal finalScore;

    @Column(name = "match_reasoning",columnDefinition = "TEXT")
    private String matchReasoning;

    @Column(name = "llm_provider",length = 50)
    private String llmProvider;

    @Column(name = "llm_model",length = 100)
    private String llmModel;

    @Column(name = "llm_prompt_version",length = 50)
    private String llmPromptVersion;

    @Column(name = "evaluated_at",nullable = false)
    private Instant evaluatedAt;


    protected JobMatch() {
        //required by JPA
    }

    public JobMatch(Job job, CandidateProfile candidateProfile, boolean hardQualified, BigDecimal skillScore, BigDecimal experienceScore, BigDecimal roleScore, BigDecimal locationScore, BigDecimal salaryScore, BigDecimal semanticScore, BigDecimal finalScore, String matchReasoning, String llmProvider, String llmModel, String llmPromptVersion, Instant evaluatedAt) {
        this.job = job;
        this.candidateProfile = candidateProfile;
        this.hardQualified = hardQualified;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.roleScore = roleScore;
        this.locationScore = locationScore;
        this.salaryScore = salaryScore;
        this.semanticScore = semanticScore;
        this.finalScore = finalScore;
        this.matchReasoning = matchReasoning;
        this.llmProvider = llmProvider;
        this.llmModel = llmModel;
        this.llmPromptVersion = llmPromptVersion;
        this.evaluatedAt = evaluatedAt;
    }

    public void update(
            boolean hardQualified,
            BigDecimal skillScore,
            BigDecimal experienceScore,
            BigDecimal roleScore,
            BigDecimal locationScore,
            BigDecimal salaryScore,
            BigDecimal semanticScore,
            BigDecimal finalScore,
            String matchReasoning,
            String llmProvider,
            String llmModel,
            String llmPromptVersion,
            Instant evaluatedAt
    ) {
        this.hardQualified = hardQualified;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.roleScore = roleScore;
        this.locationScore = locationScore;
        this.salaryScore = salaryScore;
        this.semanticScore = semanticScore;
        this.finalScore = finalScore;
        this.matchReasoning = matchReasoning;
        this.llmProvider = llmProvider;
        this.llmModel = llmModel;
        this.llmPromptVersion = llmPromptVersion;
        this.evaluatedAt = evaluatedAt;
    }

}
