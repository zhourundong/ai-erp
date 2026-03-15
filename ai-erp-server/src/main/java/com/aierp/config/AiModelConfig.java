package com.aierp.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * AI模型配置类
 * <p>
 * 支持 OpenAI 兼容的模型（包括 GPT、GLM 等）
 */
@Slf4j
@Configuration
public class AiModelConfig {

    @Value("${spring.ai.models.openai.api-key:}")
    private String openAiApiKey;

    @Value("${spring.ai.models.openai.model:gpt-4o}")
    private String openAiModel;

    @Value("${spring.ai.models.openai.base-url:}")
    private String openAiBaseUrl;

    /**
     * OpenAI模型Bean
     */
    @Bean
    public OpenAiChatModel openAiModel() {
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            log.info("OpenAI API Key 未配置，跳过模型初始化");
            return null;
        }
        log.info("初始化 OpenAI 模型: baseUrl={}, model={}", openAiBaseUrl, openAiModel);
        return OpenAiChatModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .modelName(openAiModel)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * OpenAI流式模型Bean
     */
    @Bean
    public OpenAiStreamingChatModel openAiStreamingModel() {
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            log.info("OpenAI API Key 未配置，跳过流式模型初始化");
            return null;
        }
        log.info("初始化 OpenAI 流式模型: baseUrl={}, model={}", openAiBaseUrl, openAiModel);
        return OpenAiStreamingChatModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .modelName(openAiModel)
                .timeout(Duration.ofSeconds(120))
                .logRequests(true)
                .logResponses(true)
                .customParameters(Map.of("thinking", Map.of("type", "disable")))
                .build();
    }
}
