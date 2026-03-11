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
 * 文心一言模型适配器
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.models.wenxin.enabled", havingValue = "true")
public class WenxinModelAdapter implements AIModelAdapter {

    private final AIModelConfig aiModelConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getModelName() {
        return "wenxin";
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("wenxin");
            if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
                return ChatResponse.error(request.getSessionId(), "文心一言 API Key 未配置");
            }

            // 文心一言需要先获取access_token
            String accessToken = getAccessToken(config);
            if (accessToken == null) {
                return ChatResponse.error(request.getSessionId(), "获取文心一言access_token失败");
            }

            // 构建请求体
            StringBuilder messagesJson = new StringBuilder("[");
            for (ChatRequest.Message msg : request.getHistory()) {
                messagesJson.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},",
                    msg.getRole(), escapeJson(msg.getContent())));
            }
            messagesJson.append(String.format("{\"role\":\"user\",\"content\":\"%s\"}]", escapeJson(request.getMessage())));

            String requestBody = String.format(
                "{\"messages\":%s,\"max_output_tokens\":%d,\"temperature\":%.2f}",
                messagesJson,
                config.getMaxTokens(),
                config.getTemperature()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String endpoint = config.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                endpoint = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";
            }
            endpoint = endpoint + "?access_token=" + accessToken;

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

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

                // 文心一言响应格式
                if (root.has("result")) {
                    content = root.get("result").asText();
                }

                if (root.has("usage")) {
                    JsonNode usage = root.get("usage");
                    if (usage.has("total_tokens")) {
                        tokensUsed = usage.get("total_tokens").asInt();
                    }
                }

                if (content != null) {
                    return ChatResponse.success(request.getSessionId(), content, "wenxin", tokensUsed);
                }
            }

            return ChatResponse.error(request.getSessionId(), "文心一言响应解析失败: " + response.getBody());

        } catch (Exception e) {
            log.error("文心一言调用失败: {}", e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "文心一言调用失败: " + e.getMessage());
        }
    }

    /**
     * 获取百度access_token
     */
    private String getAccessToken(AIModelConfig.ModelConfig config) {
        try {
            String url = String.format(
                "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                config.getApiKey(),
                config.getSecretKey()
            );

            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("access_token")) {
                    return root.get("access_token").asText();
                }
            }
        } catch (Exception e) {
            log.error("获取文心一言access_token失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> getCapabilities() {
        return Arrays.asList("chat");
    }

    @Override
    public void chatStream(ChatRequest request, Consumer<String> onChunk) {
        onChunk.accept("[ERROR] 此模型暂不支持流式输出");
    }

    @Override
    public boolean isAvailable() {
        AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("wenxin");
        return config != null && Boolean.TRUE.equals(config.getEnabled())
            && config.getApiKey() != null && !config.getApiKey().isEmpty()
            && config.getSecretKey() != null && !config.getSecretKey().isEmpty();
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
