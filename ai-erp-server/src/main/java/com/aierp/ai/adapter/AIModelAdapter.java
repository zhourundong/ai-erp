package com.aierp.ai.adapter;

import com.aierp.ai.model.ChatRequest;
import com.aierp.ai.model.ChatResponse;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI模型适配器接口
 */
public interface AIModelAdapter {

    /**
     * 获取模型名称
     */
    String getModelName();

    /**
     * 对话补全
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 流式对话补全
     * @param request 请求
     * @param onChunk 每次收到内容块的回调
     */
    void chatStream(ChatRequest request, Consumer<String> onChunk);

    /**
     * 获取模型能力
     */
    List<String> getCapabilities();

    /**
     * 检查是否可用
     */
    boolean isAvailable();
}
