package com.samyak.job_intelligence;

import com.samyak.job_intelligence.llm.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JobIntelligenceApplicationTests {

	@MockitoBean
	private LlmClient llmClient;
	@Test
	void contextLoads() {
	}

}
