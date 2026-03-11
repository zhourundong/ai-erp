package com.aierp.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI预测记录实体
 */
@Data
@TableName("ai_prediction")
public class AiPrediction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 预测类型: sales/inventory/purchase
     */
    private String predictionType;

    /**
     * 目标类型: product/supplier等
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 预测日期
     */
    private LocalDate predictionDate;

    /**
     * 预测值
     */
    private BigDecimal predictedValue;

    /**
     * 置信度(0-1)
     */
    private BigDecimal confidence;

    /**
     * 实际值
     */
    private BigDecimal actualValue;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
