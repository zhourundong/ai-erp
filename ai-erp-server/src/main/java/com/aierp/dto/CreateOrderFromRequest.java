package com.aierp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 从采购申请创建订单请求
 */
@Data
public class CreateOrderFromRequest {

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 采购员ID */
    private Long buyerId;

    /** 采购员姓名 */
    private String buyerName;

    /** 订单明细（为空则使用全部申请明细） */
    private List<OrderItemInput> items;

    @Data
    public static class OrderItemInput {
        /** 申请明细ID */
        private Long requestItemId;

        /** 商品ID */
        private Long productId;

        /** 商品SKU */
        private String productSku;

        /** 商品名称 */
        private String productName;

        /** 规格型号 */
        private String specification;

        /** 数量 */
        private BigDecimal quantity;

        /** 单位 */
        private String unit;

        /** 单价 */
        private BigDecimal unitPrice;
    }
}
