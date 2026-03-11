package com.aierp.ai.adapter;

import com.aierp.ai.config.AIModelConfig;
import com.aierp.ai.model.ChatRequest;
import com.aierp.ai.model.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.List;

/**
 * OpenAI模型适配器
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.models.openai.enabled", havingValue = "true")
public class OpenAIModelAdapter implements AIModelAdapter {

    private final AIModelConfig aiModelConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getModelName() {
        return "openai";
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("openai");
            if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
                return ChatResponse.error(request.getSessionId(), "OpenAI API Key 未配置");
            }

            // 构建请求体
            StringBuilder messagesJson = new StringBuilder("[");
            for (ChatRequest.Message msg : request.getHistory()) {
                messagesJson.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},",
                    msg.getRole(), escapeJson(msg.getContent())));
            }
            messagesJson.append(String.format("{\"role\":\"user\",\"content\":\"%s\"}]", escapeJson(request.getMessage())));

            String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":%s,\"max_tokens\":%d,\"temperature\":%.2f}",
                config.getModel() != null ? config.getModel() : "gpt-4",
                messagesJson,
                config.getMaxTokens(),
                config.getTemperature()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String endpoint = config.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                endpoint = "https://api.openai.com/v1/chat/completions";
            }

            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                String content = null;
                int tokensUsed = 0;

                // OpenAI响应格式
                if (root.has("choices")) {
                    JsonNode choices = root.get("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        if (choice.has("message")) {
                            content = choice.get("message").get("content").asText();
                        }
                    }
                }

                if (root.has("usage")) {
                    JsonNode usage = root.get("usage");
                    if (usage.has("total_tokens")) {
                        tokensUsed = usage.get("total_tokens").asInt();
                    }
                }

                if (content != null) {
                    return ChatResponse.success(request.getSessionId(), content, "openai", tokensUsed);
                }
            }

            return ChatResponse.error(request.getSessionId(), "OpenAI响应解析失败: " + response.getBody());

        } catch (Exception e) {
            log.error("OpenAI调用失败: {}", e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "OpenAI调用失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getCapabilities() {
        return Arrays.asList("chat", "predict", "recommend");
    }

    @Override
    public void chatStream(ChatRequest request, Consumer<String> onChunk) {
        onChunk.accept("[ERROR] 此模型暂不支持流式输出");
    }

    @Override
    public boolean isAvailable() {
        AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("openai");
        return config != null && Boolean.TRUE.equals(config.getEnabled())
            && config.getApiKey() != null && !config.getApiKey().isEmpty();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
