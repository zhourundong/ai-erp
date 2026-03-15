package com.aierp.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 响应内容
     */
    private String content;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 意图识别结果
     */
    private String intent;

    /**
     * 工具调用结果
     */
    private Object toolResult;

    /**
     * 是否需要人工确认
     */
    @Builder.Default
    private boolean requiresConfirmation = false;

    /**
     * 置信度 (0-1)
     */
    private Double confidence;

    /**
     * 处理耗时(毫秒)
     */
    private Long processingTimeMs;

    /**
     * 响应时间
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 错误信息
     */
    private String error;
}
