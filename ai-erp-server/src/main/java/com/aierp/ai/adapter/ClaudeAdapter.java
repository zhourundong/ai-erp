package com.aierp.ai.adapter;

import com.aierp.ai.ModelAdapter;
import com.aierp.ai.dto.ChatRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Claude模型适配器
 *
 * 使用Anthropic Claude API实现AI对话能力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeAdapter implements ModelAdapter {

    @Autowired(required = false)
    private AnthropicChatModel anthropicChatModel;

    @Autowired(required = false)
    @Qualifier("claudeStreamingModel")
    private StreamingChatModel anthropicStreamingChatModel;

    @Override
    public String getModelName() {
        return "claude";
    }

    @Override
    public boolean isAvailable() {
        return anthropicChatModel != null;
    }

    @Override
    public boolean isStreamingAvailable() {
        return anthropicStreamingChatModel != null;
    }

    @Override
    public com.aierp.ai.dto.ChatResponse chat(ChatRequest request) {
        if (!isAvailable()) {
            return com.aierp.ai.dto.ChatResponse.builder()
                    .error("Claude模型未配置或不可用")
                    .build();
        }

        long startTime = System.currentTimeMillis();

        try {
            // 构建消息列表
            List<ChatMessage> messages = buildMessages(request);

            // 调用Claude API (LangChain4j 1.x API)
            dev.langchain4j.model.chat.response.ChatResponse response = anthropicChatModel.chat(messages);

            long processingTime = System.currentTimeMillis() - startTime;

            return com.aierp.ai.dto.ChatResponse.builder()
                    .content(response.aiMessage().text())
                    .model(getModelName())
                    .processingTimeMs(processingTime)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Claude API调用失败", e);
            return com.aierp.ai.dto.ChatResponse.builder()
                    .error("Claude API调用失败: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public com.aierp.ai.dto.ChatResponse chatWithTools(ChatRequest request, Object... tools) {
        // TODO: 实现工具调用
        return chat(request);
    }

    @Override
    public void streamChat(ChatRequest request, StreamCallback callback) {
        if (!isStreamingAvailable()) {
            // 降级到非流式
            com.aierp.ai.dto.ChatResponse response = chat(request);
            callback.onComplete(response.getContent(), null);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 构建消息列表
            List<ChatMessage> messages = buildMessages(request);

            // 使用 AtomicReference 收集完整响应
            AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

            // 调用流式API (LangChain4j 1.x API)
            anthropicStreamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    fullResponse.get().append(partialResponse);
                    callback.onToken(partialResponse);
                }

                @Override
                public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                    long processingTime = System.currentTimeMillis() - startTime;
                    log.info("流式响应完成，耗时: {}ms, 长度: {}", processingTime, fullResponse.get().length());
                    callback.onComplete(fullResponse.get().toString(), null);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式响应错误", error);
                    callback.onError(new Exception(error));
                }
            });

        } catch (Exception e) {
            log.error("Claude流式API调用失败", e);
            callback.onError(e);
        }
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统提示
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }

        // 添加历史消息
        for (ChatRequest.Message historyMsg : request.getHistory()) {
            switch (historyMsg.getRole().toLowerCase()) {
                case "user":
                    messages.add(new UserMessage(historyMsg.getContent()));
                    break;
                case "assistant":
                    messages.add(new AiMessage(historyMsg.getContent()));
                    break;
            }
        }

        // 添加当前用户消息
        messages.add(new UserMessage(request.getMessage()));

        return messages;
    }
}
