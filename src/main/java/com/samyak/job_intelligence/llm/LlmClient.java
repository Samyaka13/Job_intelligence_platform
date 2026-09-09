package com.samyak.job_intelligence.llm;

public interface LlmClient {
    String generate(String systemPrompt,String userPrompt);
}
