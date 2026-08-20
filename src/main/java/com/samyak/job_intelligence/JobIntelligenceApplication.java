package com.samyak.job_intelligence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JobIntelligenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobIntelligenceApplication.class, args);
	}

}
