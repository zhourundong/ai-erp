package com.aierp.ai.controller;

import com.aierp.ai.entity.AiConversation;
import com.aierp.ai.model.ChatRequest;
import com.aierp.ai.model.ChatResponse;
import com.aierp.ai.service.AIService;
import com.aierp.common.result.Result;
import com.aierp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI接口控制器
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final UserMapper userMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 智能对话
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long tenantId = 1L;
        Long userId = 1L;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            // TODO: 从JWT中解析用户信息
        }

        ChatResponse response = aiService.chat(request, tenantId, userId);
        return Result.success(response);
    }

    /**
     * 流式智能对话 (SSE)
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long tenantId = 1L;
        Long userId = 1L;

        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        executor.execute(() -> {
            StringBuilder fullContent = new StringBuilder();
            try {
                aiService.chatStream(request, tenantId, userId, chunk -> {
                    try {
                        if (chunk.startsWith("[ERROR]")) {
                            emitter.send(SseEmitter.event()
                                .name("error")
                                .data(chunk.substring(7)));
                            emitter.complete();
                        } else {
                            fullContent.append(chunk);
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data(chunk));
                        }
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });

                // 保存完整对话记录
                aiService.saveConversation(tenantId, userId, request.getSessionId(),
                    "user", request.getMessage(), null, 0);
                aiService.saveConversation(tenantId, userId, request.getSessionId(),
                    "assistant", fullContent.toString(), "other", 0);

                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        return emitter;
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/chat/history/{sessionId}")
    public Result<List<AiConversation>> getHistory(@PathVariable String sessionId) {
        List<AiConversation> history = aiService.getConversationHistory(sessionId);
        return Result.success(history);
    }

    /**
     * 获取可用的AI模型列表
     */
    @GetMapping("/models")
    public Result<Map<String, Object>> getAvailableModels() {
        Map<String, Object> result = new HashMap<>();
        result.put("available", aiService.hasAvailableModel());
        result.put("models", aiService.getAvailableModels());
        return Result.success(result);
    }
}
