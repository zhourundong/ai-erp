package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水实体
 */
@Data
@TableName("inventory_transactions")
public class InventoryTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流水号 */
    private String transactionNo;

    /** 交易类型：PURCHASE_IN-采购入库, SALES_OUT-销售出库, TRANSFER-调拨, ADJUST-调整 */
    private String transactionType;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 商品ID */
    private Long productId;

    /** 商品SKU */
    private String productSku;

    /** 商品名称 */
    private String productName;

    /** 数量（正数入库，负数出库） */
    private BigDecimal quantity;

    /** 变更前数量 */
    private BigDecimal beforeQty;

    /** 变更后数量 */
    private BigDecimal afterQty;

    /** 单位成本 */
    private BigDecimal unitCost;

    /** 总成本 */
    private BigDecimal totalCost;

    /** 关联单据类型 */
    private String relatedOrderType;

    /** 关联单据ID */
    private Long relatedOrderId;

    /** 关联单据号 */
    private String relatedOrderNo;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
