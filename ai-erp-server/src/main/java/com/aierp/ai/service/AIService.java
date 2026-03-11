package com.aierp.ai.service;

import com.aierp.ai.adapter.AIModelAdapter;
import com.aierp.ai.config.AIModelConfig;
import com.aierp.ai.entity.AiConversation;
import com.aierp.ai.mapper.AiConversationMapper;
import com.aierp.ai.model.ChatRequest;
import com.aierp.ai.model.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI服务门面
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AIModelConfig aiModelConfig;
    private final AiConversationMapper conversationMapper;
    private final List<AIModelAdapter> adapters;

    private Map<String, AIModelAdapter> adapterMap;

    /**
     * 获取适配器映射
     */
    private Map<String, AIModelAdapter> getAdapterMap() {
        if (adapterMap == null) {
            adapterMap = adapters.stream()
                .collect(Collectors.toMap(AIModelAdapter::getModelName, Function.identity()));
        }
        return adapterMap;
    }

    /**
     * 智能对话
     */
    public ChatResponse chat(ChatRequest request, Long tenantId, Long userId) {
        // 生成会话ID
        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            request.setSessionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }

        // 保存用户消息
        saveConversation(tenantId, userId, request.getSessionId(), "user", request.getMessage(), null, 0);

        // 获取合适的模型适配器
        String modelName = request.getModel();
        if (modelName == null || modelName.isEmpty()) {
            modelName = aiModelConfig.getModelForCapability("chat");
        }

        AIModelAdapter adapter = getAdapter(modelName);
        if (adapter == null) {
            // 尝试备用模型
            String fallbackModel = aiModelConfig.getFallbackModel("chat");
            adapter = getAdapter(fallbackModel);
        }

        if (adapter == null) {
            return ChatResponse.error(request.getSessionId(), "没有可用的AI模型");
        }

        // 调用模型
        ChatResponse response = adapter.chat(request);

        // 保存AI回复
        if (response.getSuccess()) {
            saveConversation(tenantId, userId, request.getSessionId(), "assistant",
                response.getContent(), response.getModel(), response.getTokensUsed());
        }

        return response;
    }

    /**
     * 流式智能对话
     */
    public void chatStream(ChatRequest request, Long tenantId, Long userId, java.util.function.Consumer<String> onChunk) {
        // 生成会话ID
        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            request.setSessionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }

        // 获取合适的模型适配器
        String modelName = request.getModel();
        if (modelName == null || modelName.isEmpty()) {
            modelName = aiModelConfig.getModelForCapability("chat");
        }

        AIModelAdapter adapter = getAdapter(modelName);
        if (adapter == null) {
            String fallbackModel = aiModelConfig.getFallbackModel("chat");
            adapter = getAdapter(fallbackModel);
        }

        if (adapter == null) {
            onChunk.accept("[ERROR] 没有可用的AI模型");
            return;
        }

        // 调用流式模型
        adapter.chatStream(request, onChunk);
    }

    /**
     * 获取适配器
     */
    private AIModelAdapter getAdapter(String modelName) {
        AIModelAdapter adapter = getAdapterMap().get(modelName);
        if (adapter != null && adapter.isAvailable()) {
            return adapter;
        }
        return null;
    }

    /**
     * 保存对话记录
     */
    public void saveConversation(Long tenantId, Long userId, String sessionId,
                                   String role, String content, String model, Integer tokensUsed) {
        try {
            AiConversation conversation = new AiConversation();
            conversation.setTenantId(tenantId);
            conversation.setUserId(userId);
            conversation.setSessionId(sessionId);
            conversation.setRole(role);
            conversation.setContent(content);
            conversation.setModel(model);
            conversation.setTokensUsed(tokensUsed != null ? tokensUsed : 0);
            conversation.setCreatedTime(LocalDateTime.now());
            conversationMapper.insert(conversation);
        } catch (Exception e) {
            log.error("保存对话记录失败: {}", e.getMessage());
        }
    }

    /**
     * 获取会话历史
     */
    public List<AiConversation> getConversationHistory(String sessionId) {
        return conversationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getSessionId, sessionId)
                .orderByAsc(AiConversation::getCreatedTime)
        );
    }

    /**
     * 检查是否有可用的模型
     */
    public boolean hasAvailableModel() {
        return getAdapterMap().values().stream().anyMatch(AIModelAdapter::isAvailable);
    }

    /**
     * 获取可用的模型列表
     */
    public List<String> getAvailableModels() {
        return getAdapterMap().entrySet().stream()
            .filter(e -> e.getValue().isAvailable())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
