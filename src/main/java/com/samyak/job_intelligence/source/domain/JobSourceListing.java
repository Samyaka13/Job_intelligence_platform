package com.samyak.job_intelligence.source.domain;


import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;

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


}
