package com.samyak.job_intelligence.job.domain;

import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "job_locations",indexes = {
        @Index(name = "idx_job_locations_job_id", columnList = "job_id"),
        @Index(name = "idx_job_locations_country",columnList = "country"),
        @Index(name = "idx_job_locations_city",columnList = "city"),
        @Index(name = "idx_job_locations_remote_type",columnList = "remote_type"),
})
public class JobLocation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id",nullable = false, foreignKey = @ForeignKey(name = "fk_job_locations_job"))
    private Job job;

    @Column(length = 150)
    private String city;

    @Column(length = 150)
    private String state;

    @Column(length = 150)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "remote_type",length = 30,nullable = false)
    private RemoteType remoteType;

    @Column(name = "display_text",length = 500,nullable = false)
    private String displayText;

    protected JobLocation() {
        //required by JPA
    }


    public JobLocation(Job job, String city, String state, String country, RemoteType remoteType, String displayText) {
        this.job = job;
        this.city = city;
        this.state = state;
        this.country = country;
        this.remoteType = remoteType;
        this.displayText = displayText;
    }
}
