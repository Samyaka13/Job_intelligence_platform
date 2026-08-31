package com.samyak.job_intelligence.source.domain;


import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import com.samyak.job_intelligence.job.domain.Job;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Getter
@Entity
@Table(name = "job_source_listings",indexes = {@Index(name = "idx_job_source_listings_job_id",columnList = "job_id"),
        @Index(name = "idx_job_source_listings_source_id",columnList = "job_source_id"),
        @Index(name = "idx_job_source_listings_last_seen_at",columnList = "last_seen_at"),
        @Index(name = "idx_job_source_listings_active",columnList = "is_active")
})
public class JobSourceListing extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "job_id",nullable = false,foreignKey = @ForeignKey(name = "fk_job_source_listings_job"))
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "job_source_id",nullable = false,foreignKey = @ForeignKey(name = "fk_job_source_listings_source"))
    private JobSource jobSource;

    @Column(name = "external_job_id",length = 255)
    private String externalJobId ;

    @Column(name = "source_url", length = 2048,nullable = false)
    private String sourceUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload" ,columnDefinition = "jsonb")
    private JsonNode rawPayload;

    @Column(name = "source_posted_at")
    private Instant sourcePostedAt;

    @Column(name = "discovered_at",nullable = false)
    private Instant discoveredAt;

    @Column(name = "last_seen_at",nullable = false)
    private Instant lastSeenAt;

    @Column(name = "is_active",nullable = false)
    private boolean isActive = true;

   protected JobSourceListing(){
       //required by JPA
   }

    public JobSourceListing(
            Job job,
            JobSource jobSource,
            String externalJobId,
            String sourceUrl,
            JsonNode rawPayload,
            Instant sourcePostedAt
    ) {
        this.job = job;
        this.jobSource = jobSource;
        this.externalJobId = externalJobId;
        this.sourceUrl = sourceUrl;
        this.rawPayload = rawPayload;
        this.sourcePostedAt = sourcePostedAt;

        Instant now = Instant.now();
        this.discoveredAt = now;
        this.lastSeenAt = now;
    }

    public void refresh(String sourceUrl,JsonNode rawPayload,Instant sourcePostedAt){
       this.sourceUrl = sourceUrl;
       this.rawPayload = rawPayload;
       this.sourcePostedAt = sourcePostedAt;
       this.isActive = true;
    }

}
