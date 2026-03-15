package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体
 */
@Data
@TableName("inventories")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 仓库ID */
    private Long warehouseId;

    /** 商品ID */
    private Long productId;

    /** 商品SKU */
    private String productSku;

    /** 商品名称 */
    private String productName;

    /** 库存数量 */
    private BigDecimal quantity;

    /** 可用数量 */
    private BigDecimal availableQty;

    /** 锁定数量 */
    private BigDecimal lockedQty;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 最后入库时间 */
    private LocalDateTime lastInDate;

    /** 最后出库时间 */
    private LocalDateTime lastOutDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
