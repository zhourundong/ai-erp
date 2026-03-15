package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购申请明细实体
 */
@Data
@TableName("purchase_request_items")
public class PurchaseRequestItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请单ID */
    private Long requestId;

    /** 商品ID */
    private Long productId;

    /** 商品SKU */
    private String productSku;

    /** 商品名称 */
    private String productName;

    /** 规格型号 */
    private String specification;

    /** 分类 */
    private String category;

    /** 数量 */
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 预估单价 */
    private BigDecimal estimatedPrice;

    /** 预估金额 */
    private BigDecimal estimatedAmount;

    /** 需求日期 */
    private LocalDate requiredDate;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
