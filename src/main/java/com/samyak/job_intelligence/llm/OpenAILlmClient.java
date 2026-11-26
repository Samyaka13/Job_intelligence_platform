package com.samyak.job_intelligence.llm;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig;
import com.openai.models.responses.ResponseTextConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.stream.Collectors;

public class OpenAILlmClient implements LlmClient{

    private static final Logger log =
            LoggerFactory.getLogger(OpenAILlmClient.class);
    private final OpenAIClient openAIClient;
    private final OpenAiProperties openAiProperties;
    private final OpenAIRequirementSchema openAIRequirementSchema;

    public OpenAILlmClient(OpenAiProperties openAiProperties,OpenAIRequirementSchema openAIRequirementSchema) {
        this.openAIClient = OpenAIOkHttpClient.fromEnv();
        this.openAiProperties = openAiProperties;
        this.openAIRequirementSchema = openAIRequirementSchema;
    }
    @Override
    public String generate(String systemPrompt,String userPrompt) {
        ResponseTextConfig textConfig = ResponseTextConfig.builder()
                .format(
                        ResponseFormatTextJsonSchemaConfig.builder()
                                .name("job_requirements")
                                .schema(openAIRequirementSchema.build())
                                .strict(true)
                                .build()
                )
                .build();

        ResponseCreateParams responseCreateParams = ResponseCreateParams.builder()
                .model(openAiProperties.modelName())
                .instructions(systemPrompt)
                .input(userPrompt)
                .text(textConfig)
                .build();

        Response response = openAIClient.responses().create(responseCreateParams);
        response.usage().ifPresent(usage -> {

            long inputTokens =
                    Optional.ofNullable(usage.inputTokens()).orElse(0L);

            long outputTokens =
                    Optional.ofNullable(usage.outputTokens()).orElse(0L);

            long cachedInputTokens =
                    Optional.ofNullable(usage.inputTokensDetails())
                            .map(details -> details.cachedTokens())
                            .orElse(0L);

            long totalTokens =
                    Optional.ofNullable(usage.totalTokens()).orElse(0L);

            log.info(
                    "OPENAI USAGE | job={} | input={} | output={} | cached={} | total={}",
                    MDC.get("jobId"),
                    inputTokens,
                    outputTokens,
                    cachedInputTokens,
                    totalTokens
            );
        });
        String output = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .collect(Collectors.joining());
        return output;
    }
}
