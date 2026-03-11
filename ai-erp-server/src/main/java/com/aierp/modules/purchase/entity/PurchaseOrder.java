package com.aierp.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单实体
 */
@Data
@TableName("purchase_order")
public class PurchaseOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 订单号 */
    private String orderNo;
    
    /** 供应商ID */
    private Long supplierId;
    
    /** 预计到货日期 */
    private LocalDateTime expectedArrivalDate;
    
    /** 仓库ID */
    private Long warehouseId;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 订单状态 0-草稿 1-待审批 2-已审批 3-部分入库 4-已完成 5-已取消 */
    private Integer status;
    
    /** AI建议(JSON格式) */
    private String aiSuggestion;
    
    /** 备注 */
    private String remark;
    
    /** 创建人 */
    private Long createdBy;
    
    /** 审批人 */
    private Long approvedBy;
    
    /** 审批时间 */
    private LocalDateTime approvedTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
}
