package com.samyak.job_intelligence;

import com.samyak.job_intelligence.llm.GeminiProperties;
import com.samyak.job_intelligence.source.greehouse.GreenhouseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableConfigurationProperties({GreenhouseProperties.class, GeminiProperties.class})
@SpringBootApplication
public class JobIntelligenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobIntelligenceApplication.class, args);
	}

}
