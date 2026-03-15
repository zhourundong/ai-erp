package com.aierp.ai;

import com.aierp.ai.dto.ChatRequest;
import com.aierp.ai.dto.ChatResponse;

/**
 * AI模型适配器接口
 *
 * 支持多种AI模型的统一调用接口
 * 实现：ClaudeAdapter, OpenAIAdapter
 */
public interface ModelAdapter {

    /**
     * 获取模型名称
     */
    String getModelName();

    /**
     * 检查模型是否可用
     */
    boolean isAvailable();

    /**
     * 检查流式模型是否可用
     */
    boolean isStreamingAvailable();

    /**
     * 发送聊天请求
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 发送聊天请求并支持工具调用
     *
     * @param request 聊天请求
     * @param tools 可用工具列表
     * @return 聊天响应
     */
    ChatResponse chatWithTools(ChatRequest request, Object... tools);

    /**
     * 流式聊天（用于长响应）
     *
     * @param request 聊天请求
     * @param callback 流式回调
     */
    void streamChat(ChatRequest request, StreamCallback callback);

    /**
     * 流式回调接口
     */
    interface StreamCallback {
        /**
         * 收到思考过程（reasoning_content）
         */
        default void onThinking(String thinking) {}

        /**
         * 收到实际内容
         */
        void onToken(String token);

        /**
         * 流式完成
         * @param fullResponse 完整响应内容
         * @param thinkingTimeMs 思考耗时(毫秒)，可能为null
         */
        void onComplete(String fullResponse, Long thinkingTimeMs);

        /**
         * 发生错误
         */
        void onError(Exception e);
    }
}
