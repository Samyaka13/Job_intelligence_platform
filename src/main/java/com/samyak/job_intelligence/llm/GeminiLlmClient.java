package com.samyak.job_intelligence.llm;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import org.springframework.stereotype.Component;

@Component
public class GeminiLlmClient implements LlmClient {

    private final Client client;
    private final GeminiProperties geminiProperties;
    private final GeminiRequirementSchema requirementSchema;

    public GeminiLlmClient(GeminiProperties geminiProperties,GeminiRequirementSchema requirementSchema) {
        this.client = Client.builder().apiKey(geminiProperties.apiKey()).build();
        this.geminiProperties = geminiProperties;
        this.requirementSchema = requirementSchema;
    }


    @Override
    public String generate(String systemPrompt, String userPrompt) {
        Content content = Content.builder().role("system").parts(Part.fromText(systemPrompt)).build();
        GenerateContentConfig config = GenerateContentConfig
                .builder().
                systemInstruction(content)
                .responseMimeType("application/json")
                .responseSchema(requirementSchema.build())
                .build();
        var response = client.models.generateContent(geminiProperties.model(),userPrompt,config);
        return response.text();
    }
}
