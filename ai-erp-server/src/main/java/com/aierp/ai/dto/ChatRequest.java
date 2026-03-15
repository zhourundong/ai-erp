package com.aierp.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户消息
     */
    private String message;

    /**
     * 会话ID（用于保持上下文）
     */
    private String sessionId;

    /**
     * 历史消息
     */
    @Builder.Default
    private List<Message> history = new ArrayList<>();

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 温度参数 (0-1)
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    @Builder.Default
    private Integer maxTokens = 4096;

    /**
     * 消息对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role; // user, assistant, system
        private String content;
    }
}
