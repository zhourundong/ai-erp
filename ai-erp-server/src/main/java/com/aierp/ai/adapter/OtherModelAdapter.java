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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 其他模型适配器 (OpenAI协议兼容)
 * 用于支持任何遵循 OpenAI 协议的 API
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.models.other.enabled", havingValue = "true")
public class OtherModelAdapter implements AIModelAdapter {

    private final AIModelConfig aiModelConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getModelName() {
        return "other";
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("other");
            if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
                return ChatResponse.error(request.getSessionId(), "Other模型 API Key 未配置");
            }

            // 构建请求体 (OpenAI 协议格式)
            StringBuilder messagesJson = new StringBuilder("[");
            if (request.getHistory() != null) {
                for (ChatRequest.Message msg : request.getHistory()) {
                    messagesJson.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},",
                        msg.getRole(), escapeJson(msg.getContent())));
                }
            }
            messagesJson.append(String.format("{\"role\":\"user\",\"content\":\"%s\"}]", escapeJson(request.getMessage())));

            String modelName = config.getModel() != null ? config.getModel() : "default";
            String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":%s,\"max_tokens\":%d,\"temperature\":%.2f}",
                modelName,
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
                endpoint = "https://qianfan.baidubce.com/v2/coding/chat/completions";
            }

            log.debug("调用 Other 模型: endpoint={}, model={}", endpoint, modelName);

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

                // OpenAI 协议响应格式
                if (root.has("choices")) {
                    JsonNode choices = root.get("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        if (choice.has("message")) {
                            JsonNode message = choice.get("message");
                            if (message.has("content")) {
                                content = message.get("content").asText();
                            }
                        } else if (choice.has("text")) {
                            // 部分兼容格式
                            content = choice.get("text").asText();
                        }
                    }
                }

                // 解析 usage
                if (root.has("usage")) {
                    JsonNode usage = root.get("usage");
                    if (usage.has("total_tokens")) {
                        tokensUsed = usage.get("total_tokens").asInt();
                    } else if (usage.has("prompt_tokens") && usage.has("completion_tokens")) {
                        tokensUsed = usage.get("prompt_tokens").asInt() + usage.get("completion_tokens").asInt();
                    }
                }

                if (content != null) {
                    return ChatResponse.success(request.getSessionId(), content, "other", tokensUsed);
                }
            }

            log.error("Other模型响应解析失败: {}", response.getBody());
            return ChatResponse.error(request.getSessionId(), "Other模型响应解析失败: " + response.getBody());

        } catch (Exception e) {
            log.error("Other模型调用失败: {}", e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "Other模型调用失败: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(ChatRequest request, Consumer<String> onChunk) {
        try {
            AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("other");
            if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
                onChunk.accept("[ERROR] Other模型 API Key 未配置");
                return;
            }

            // 构建请求体 (OpenAI 协议格式，启用流式)
            StringBuilder messagesJson = new StringBuilder("[");
            if (request.getHistory() != null) {
                for (ChatRequest.Message msg : request.getHistory()) {
                    messagesJson.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},",
                        msg.getRole(), escapeJson(msg.getContent())));
                }
            }
            messagesJson.append(String.format("{\"role\":\"user\",\"content\":\"%s\"}]", escapeJson(request.getMessage())));

            String modelName = config.getModel() != null ? config.getModel() : "default";
            String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":%s,\"max_tokens\":%d,\"temperature\":%.2f,\"stream\":true}",
                modelName,
                messagesJson,
                config.getMaxTokens(),
                config.getTemperature()
            );

            String endpoint = config.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                endpoint = "https://qianfan.baidubce.com/v2/coding/chat/completions";
            }

            log.debug("调用 Other 模型(流式): endpoint={}, model={}", endpoint, modelName);

            // 使用 HttpURLConnection 发送流式请求
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            conn.getOutputStream().write(requestBody.getBytes("UTF-8"));
            conn.getOutputStream().flush();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                String line;
                StringBuilder errorResponse = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                onChunk.accept("[ERROR] 请求失败: " + errorResponse.toString());
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    try {
                        JsonNode root = objectMapper.readTree(data);
                        if (root.has("choices")) {
                            JsonNode choices = root.get("choices");
                            if (choices.isArray() && choices.size() > 0) {
                                JsonNode choice = choices.get(0);
                                JsonNode delta = choice.get("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").asText();
                                    if (content != null && !content.isEmpty()) {
                                        onChunk.accept(content);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("解析SSE数据失败: {}", data);
                    }
                }
            }
            reader.close();
            conn.disconnect();

        } catch (Exception e) {
            log.error("Other模型流式调用失败: {}", e.getMessage(), e);
            onChunk.accept("[ERROR] " + e.getMessage());
        }
    }

    @Override
    public List<String> getCapabilities() {
        return Arrays.asList("chat", "predict", "recommend");
    }

    @Override
    public boolean isAvailable() {
        AIModelConfig.ModelConfig config = aiModelConfig.getModelConfig("other");
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
