package com.aierp.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话记录实体
 */
@Data
@TableName("ai_conversation")
public class AiConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 角色: user/assistant
     */
    private String role;

    /**
     * 内容
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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
