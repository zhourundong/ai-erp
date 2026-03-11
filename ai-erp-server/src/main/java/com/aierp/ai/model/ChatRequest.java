package com.aierp.ai.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI对话请求
 */
@Data
public class ChatRequest {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 历史消息
     */
    private List<Message> history = new ArrayList<>();

    /**
     * 指定模型(可选)
     */
    private String model;

    /**
     * 消息
     */
    @Data
    public static class Message {
        private String role;  // user/assistant/system
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
