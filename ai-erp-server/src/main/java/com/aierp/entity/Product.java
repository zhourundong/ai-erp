package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品/物料实体
 */
@Data
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品编码SKU */
    private String sku;

    /** 商品名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 销售价 */
    private BigDecimal salePrice;

    /** 市场价 */
    private BigDecimal marketPrice;

    /** 安全库存 */
    private Integer safetyStock;

    /** 最大库存 */
    private Integer maxStock;

    /** 最小订货量 */
    private Integer minOrderQty;

    /** 条码 */
    private String barcode;

    /** 图片URL */
    private String imageUrl;

    /** 状态：ACTIVE-启用, INACTIVE-停用 */
    private String status;

    /** 描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
