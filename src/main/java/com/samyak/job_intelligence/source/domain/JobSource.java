package com.samyak.job_intelligence.source.domain;


import com.samyak.job_intelligence.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;


@Getter
@Entity
@Table(name = "job_sources")
public class JobSource extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false,length = 50,unique = true)
    private String code;

    @Column(name = "display_name",nullable = false,length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type",nullable = false,length = 50)
    private JobSourceType sourceType;

    @Column(name = "base_url",length = 2048)
    private String baseUrl;

    @Column(nullable = false)
    private boolean enabled = true;

    protected JobSource(){
        //required by JPA
    }

    public JobSource(String code,String displayName,JobSourceType sourceType,String baseUrl){
        this.code = code;
        this.displayName = displayName;
        this.sourceType = sourceType;
        this.baseUrl = baseUrl;
    }


}
