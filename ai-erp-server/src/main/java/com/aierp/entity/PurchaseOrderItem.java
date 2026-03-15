package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单明细实体
 */
@Data
@TableName("purchase_order_items")
public class PurchaseOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

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

    /** 金额 */
    private BigDecimal amount;

    /** 税率 */
    private BigDecimal taxRate;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 已收货数量 */
    private BigDecimal receivedQty;

    /** 状态 */
    private String status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
