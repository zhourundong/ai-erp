package com.aierp.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单明细实体
 */
@Data
@TableName("purchase_order_item")
public class PurchaseOrderItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 采购订单ID */
    private Long orderId;
    
    /** 商品ID */
    private Long productId;
    
    /** 商品名称 */
    private String productName;
    
    /** 单位 */
    private String unit;
    
    /** 数量 */
    private BigDecimal quantity;
    
    /** 单价 */
    private BigDecimal unitPrice;
    
    /** 金额 */
    private BigDecimal amount;
    
    /** 已入库数量 */
    private BigDecimal receivedQuantity;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
