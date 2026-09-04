package com.samyak.job_intelligence.candidate.domain;


import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "candidate_skills" ,indexes = {
        @Index(name = "idx_candidate_skills_candidate_id",columnList = "candidate_profile_id"),
        @Index(name = "idx_candidate_skills_normalized_skill",columnList = "normalized_skill"),
        @Index(name = "idx_candidate_skills_primary",columnList = "is_primary")
})
public class CandidateSkill extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(
            name = "candidate_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_candidate_skills_candidate")
    )
    private CandidateProfile candidateProfile;

    @Column(nullable = false,length = 255)
    private String skill;

    @Column(name = "normalized_skill",length = 255,nullable = false)
    private String normalizedSkill;

    @Column(length = 30)
    String proficiency;

    @Column(name = "years_experience",precision = 4,scale = 1)
    private BigDecimal yearsExperience ;

    @Column(name = "is_primary",nullable = false)
    private boolean primary;

    protected CandidateSkill(){
        // required by JPA
    }

    public CandidateSkill(
            CandidateProfile candidateProfile,
            String skill,
            String normalizedSkill,
            String proficiency,
            BigDecimal yearsExperience,
            boolean primary
    ) {
        this.candidateProfile = candidateProfile;
        this.skill = skill;
        this.normalizedSkill = normalizedSkill;
        this.proficiency = proficiency;
        this.yearsExperience = yearsExperience;
        this.primary = primary;
    }




}
