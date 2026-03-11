package com.aierp.ai.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * AI预测响应
 */
@Data
public class PredictionResponse {

    /**
     * 预测类型: sales/inventory/purchase
     */
    private String predictionType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 预测结果列表
     */
    private List<PredictionItem> predictions;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 预测项
     */
    @Data
    public static class PredictionItem {
        private LocalDate date;
        private BigDecimal value;
        private BigDecimal confidence;
        private String trend;  // up/down/stable
    }
}
