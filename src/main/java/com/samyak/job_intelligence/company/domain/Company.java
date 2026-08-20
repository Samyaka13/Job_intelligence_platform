package com.samyak.job_intelligence.company.domain;

import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "companies")
public class Company extends AuditableEntity {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
    @Column(name = "canonical_name",nullable = false,unique = true,length = 255)
    private String canonicalName;

    @Column(name = "display_name",nullable = false,length = 255)
    private String displayName;

    @Column(name = "website_url" ,length = 2048)
    private String websiteUrl;

    protected Company(){
        //required by JPA
    }

    public Company(String canonicalName,String displayName,String websiteUrl){
        this.canonicalName = canonicalName;
        this.displayName = displayName;
        this.websiteUrl = websiteUrl;
    }

    public Long getId(){
        return id;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

}
