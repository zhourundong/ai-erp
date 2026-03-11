package com.aierp.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 商品编码 */
    private String productCode;
    
    /** 商品名称 */
    private String productName;
    
    /** 分类ID */
    private Long categoryId;
    
    /** 品牌 */
    private String brand;
    
    /** 单位 */
    private String unit;
    
    /** 规格 */
    private String specification;
    
    /** 成本价 */
    private BigDecimal costPrice;
    
    /** 销售价 */
    private BigDecimal salePrice;
    
    /** 安全库存天数 */
    private Integer safetyStockDays;
    
    /** 采购周期(天) */
    private Integer purchaseLeadTime;
    
    /** 状态 1-启用 0-停用 */
    private Integer status;
    
    /** 是否启用AI预测 */
    private Integer aiPredictionEnabled;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
}
