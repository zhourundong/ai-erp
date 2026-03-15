package com.aierp.ai.adapter;

import com.aierp.ai.ModelAdapter;
import com.aierp.ai.dto.ChatRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * OpenAI模型适配器
 *
 * 使用OpenAI GPT API实现AI对话能力
 */
@Slf4j
@Component
public class OpenAIAdapter implements ModelAdapter {

    @Autowired(required = false)
    private OpenAiChatModel openAiChatModel;

    @Autowired(required = false)
    @Qualifier("openAiStreamingModel")
    private StreamingChatModel openAiStreamingChatModel;

    /**
     * 初始化后检查配置状态
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("OpenAIAdapter 初始化: chatModel={}, streamingModel={}",
            openAiChatModel != null ? "已配置" : "未配置",
            openAiStreamingChatModel != null ? "已配置" : "未配置");
    }

    @Override
    public String getModelName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return openAiChatModel != null;
    }

    @Override
    public boolean isStreamingAvailable() {
        return openAiStreamingChatModel != null;
    }

    @Override
    public com.aierp.ai.dto.ChatResponse chat(ChatRequest request) {
        if (!isAvailable()) {
            return com.aierp.ai.dto.ChatResponse.builder()
                    .error("OpenAI模型未配置或不可用")
                    .build();
        }

        long startTime = System.currentTimeMillis();

        try {
            // 构建消息列表
            List<ChatMessage> messages = buildMessages(request);

            // 调用OpenAI API (LangChain4j 1.x API)
            dev.langchain4j.model.chat.response.ChatResponse response = openAiChatModel.chat(messages);

            long processingTime = System.currentTimeMillis() - startTime;

            return com.aierp.ai.dto.ChatResponse.builder()
                    .content(response.aiMessage().text())
                    .model(getModelName())
                    .processingTimeMs(processingTime)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OpenAI API调用失败", e);
            return com.aierp.ai.dto.ChatResponse.builder()
                    .error("OpenAI API调用失败: " + e.getMessage())
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
        log.info("streamChat called, isStreamingAvailable: {}", isStreamingAvailable());

        if (!isStreamingAvailable()) {
            log.warn("流式模型不可用，降级到非流式调用");
            // 降级到非流式
            com.aierp.ai.dto.ChatResponse response = chat(request);
            // 模拟流式输出，分块发送
            String content = response.getContent();
            if (content != null && !content.isEmpty()) {
                int chunkSize = 20;
                for (int i = 0; i < content.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, content.length());
                    String chunk = content.substring(i, end);
                    callback.onToken(chunk);
                    try {
                        Thread.sleep(50); // 模拟延迟
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            callback.onComplete(content);
            return;
        }

        long startTime = System.currentTimeMillis();
        final int[] tokenCount = {0};
        final boolean[] receivedFirstThinking = {false};
        final boolean[] receivedFirstContent = {false};

        try {
            // 构建消息列表
            List<ChatMessage> messages = buildMessages(request);
            log.info("构建消息完成，消息数量: {}, 开始调用流式API...", messages.size());

            // 使用 AtomicReference 收集完整响应
            AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

            // 调用流式API (LangChain4j 1.x API)
            openAiStreamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    if (!receivedFirstContent[0]) {
                        receivedFirstContent[0] = true;
                        log.info("收到第一个content token (耗时: {}ms)", System.currentTimeMillis() - startTime);
                    }
                    tokenCount[0]++;
                    fullResponse.get().append(partialResponse);
                    callback.onToken(partialResponse);
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking) {
                    // 处理 reasoning_content（推理过程）
                    if (!receivedFirstThinking[0]) {
                        receivedFirstThinking[0] = true;
                        log.info("收到第一个thinking token (耗时: {}ms)", System.currentTimeMillis() - startTime);
                    }
                    tokenCount[0]++;
                    // 从 PartialThinking 中提取 text
                    if (partialThinking != null) {
                        String thinkingText = partialThinking.text();
                        if (thinkingText != null && !thinkingText.isEmpty()) {
                            callback.onThinking(thinkingText);
                        }
                    }
                }

                @Override
                public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                    long processingTime = System.currentTimeMillis() - startTime;
                    log.info("流式响应完成，耗时: {}ms, token数: {}, 内容长度: {}",
                        processingTime, tokenCount[0], fullResponse.get().length());
                    callback.onComplete(fullResponse.get().toString());
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式响应错误", error);
                    callback.onError(new Exception(error));
                }
            });

            log.info("streamChat 方法返回（异步调用已发起）");

        } catch (Exception e) {
            log.error("OpenAI流式API调用失败", e);
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
        if (request.getHistory() != null) {
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
        }

        // 添加当前用户消息
        messages.add(new UserMessage(request.getMessage()));

        return messages;
    }
}
