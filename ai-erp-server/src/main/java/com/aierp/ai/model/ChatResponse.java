package com.aierp.ai.model;

import lombok.Data;

/**
 * AI对话响应
 */
@Data
public class ChatResponse {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * AI回复内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * Token消耗
     */
    private Integer tokensUsed;

    /**
     * 是否成功
     */
    private Boolean success = true;

    /**
     * 错误信息
     */
    private String errorMessage;

    public static ChatResponse success(String sessionId, String content, String model, Integer tokensUsed) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setContent(content);
        response.setModel(model);
        response.setTokensUsed(tokensUsed);
        response.setSuccess(true);
        return response;
    }

    public static ChatResponse error(String sessionId, String errorMessage) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
