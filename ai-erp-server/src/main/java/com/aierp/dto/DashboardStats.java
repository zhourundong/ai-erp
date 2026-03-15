package com.aierp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 驾驶舱统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    // ========== 核心指标 ==========

    /**
     * 采购订单总数
     */
    private Long purchaseOrderCount;

    /**
     * 销售订单总数
     */
    private Long salesOrderCount;

    /**
     * 商品总数
     */
    private Long productCount;

    /**
     * 供应商总数
     */
    private Long supplierCount;

    /**
     * 客户总数
     */
    private Long customerCount;

    /**
     * 仓库总数
     */
    private Long warehouseCount;

    // ========== 金额统计 ==========

    /**
     * 采购总金额
     */
    private BigDecimal totalPurchaseAmount;

    /**
     * 销售总金额
     */
    private BigDecimal totalSalesAmount;

    // ========== 订单状态分布 ==========

    /**
     * 采购订单状态分布
     */
    private List<StatusCount> purchaseOrderStatus;

    /**
     * 销售订单状态分布
     */
    private List<StatusCount> salesOrderStatus;

    // ========== 库存预警 ==========

    /**
     * 库存预警数量（低于安全库存）
     */
    private Long inventoryWarningCount;

    /**
     * 库存预警列表
     */
    private List<InventoryWarning> inventoryWarnings;

    // ========== 近期趋势 ==========

    /**
     * 近7天采购订单趋势
     */
    private List<DailyTrend> purchaseTrend;

    /**
     * 近7天销售订单趋势
     */
    private List<DailyTrend> salesTrend;

    // ========== 待办事项 ==========

    /**
     * 待审批采购订单数
     */
    private Long pendingApprovalCount;

    /**
     * 待收货采购订单数
     */
    private Long pendingReceiveCount;

    /**
     * 待发货销售订单数
     */
    private Long pendingShipCount;

    /**
     * 状态统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private String status;
        private String label;
        private Long count;
    }

    /**
     * 库存预警
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryWarning {
        private Long productId;
        private String productSku;
        private String productName;
        private Integer safetyStock;
        private Integer currentStock;
        private String warehouseName;
    }

    /**
     * 每日趋势
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;
        private Long count;
        private BigDecimal amount;
    }
}
