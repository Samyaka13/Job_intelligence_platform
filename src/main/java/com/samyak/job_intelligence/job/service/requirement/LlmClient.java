package com.samyak.job_intelligence.job.service.requirement;

public interface LlmClient {
    String generate(String systemPrompt,String userPrompt);
}
