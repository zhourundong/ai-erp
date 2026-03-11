package com.aierp.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体
 */
@Data
@TableName("inventory")
public class Inventory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 商品ID */
    private Long productId;
    
    /** 仓库ID */
    private Long warehouseId;
    
    /** 库存数量 */
    private BigDecimal quantity;
    
    /** 可用数量 */
    private BigDecimal availableQuantity;
    
    /** 锁定数量 */
    private BigDecimal lockedQuantity;
    
    /** 预警阈值(固定) */
    private BigDecimal warningThreshold;
    
    /** AI动态预警阈值 */
    private BigDecimal aiWarningThreshold;
    
    /** AI预警状态 0-正常 1-预警 */
    private Integer aiWarningStatus;
    
    /** 最后入库时间 */
    private LocalDateTime lastInTime;
    
    /** 最后出库时间 */
    private LocalDateTime lastOutTime;
    
    /** 库存周转天数 */
    private Integer inventoryTurnoverDays;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
