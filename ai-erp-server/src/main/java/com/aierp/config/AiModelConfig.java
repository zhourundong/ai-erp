package com.aierp.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
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
 * 支持多模型切换：
 * - Claude (Anthropic)
 * - OpenAI (GPT)
 */
@Slf4j
@Configuration
public class AiModelConfig {

    @Value("${spring.ai.models.claude.api-key:}")
    private String claudeApiKey;

    @Value("${spring.ai.models.claude.model:claude-sonnet-4-6-20250514}")
    private String claudeModel;

    @Value("${spring.ai.models.openai.api-key:}")
    private String openAiApiKey;

    @Value("${spring.ai.models.openai.model:gpt-4o}")
    private String openAiModel;

    @Value("${spring.ai.models.openai.base-url:}")
    private String openAiBaseUrl;

    /**
     * Claude模型Bean
     */
    @Bean
    public AnthropicChatModel claudeModel() {
        if (claudeApiKey == null || claudeApiKey.isEmpty()) {
            log.info("Claude API Key 未配置，跳过 Claude 模型初始化");
            return null; // 未配置API Key时返回null
        }
        log.info("初始化 Claude 模型: {}", claudeModel);
        return AnthropicChatModel.builder()
                .apiKey(claudeApiKey)
                .modelName(claudeModel)
                .timeout(Duration.ofSeconds(60))
                .maxTokens(4096)
                .build();
    }

    /**
     * Claude流式模型Bean
     */
    @Bean
    public AnthropicStreamingChatModel claudeStreamingModel() {
        if (claudeApiKey == null || claudeApiKey.isEmpty()) {
            return null;
        }
        log.info("初始化 Claude 流式模型: {}", claudeModel);
        return AnthropicStreamingChatModel.builder()
                .apiKey(claudeApiKey)
                .modelName(claudeModel)
                .timeout(Duration.ofSeconds(60))
                .maxTokens(4096)
                .build();
    }

    /**
     * OpenAI模型Bean
     */
    @Bean
    public OpenAiChatModel openAiModel() {
        if (openAiApiKey == null || openAiApiKey.isEmpty()) {
            log.info("OpenAI API Key 未配置，跳过 OpenAI 模型初始化");
            return null; // 未配置API Key时返回null
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
            log.info("OpenAI API Key 未配置，跳过 OpenAI 流式模型初始化");
            return null;
        }
        log.info("初始化 OpenAI 流式模型: baseUrl={}, model={}", openAiBaseUrl, openAiModel);
        return OpenAiStreamingChatModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .modelName(openAiModel)
                .timeout(Duration.ofSeconds(120))
//                    .returnThinking(true)  // 支持 reasoning_content
                .logRequests(true)
                .logResponses(true)
                .customParameters(Map.of("thinking", Map.of("type", "disable")))
                .build();
    }
}
