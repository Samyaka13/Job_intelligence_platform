package com.samyak.job_intelligence;

import org.springframework.boot.SpringApplication;

public class TestJobIntelligenceApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobIntelligenceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
