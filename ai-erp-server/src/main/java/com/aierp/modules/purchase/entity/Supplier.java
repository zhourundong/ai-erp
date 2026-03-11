package com.aierp.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
@TableName("supplier")
public class Supplier {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 供应商编码 */
    private String supplierCode;
    
    /** 供应商名称 */
    private String supplierName;
    
    /** 联系人 */
    private String contactName;
    
    /** 联系电话 */
    private String contactPhone;
    
    /** 联系邮箱 */
    private String contactEmail;
    
    /** 地址 */
    private String address;
    
    /** 综合评分 */
    private BigDecimal score;
    
    /** 价格评分 */
    private BigDecimal priceScore;
    
    /** 交期评分 */
    private BigDecimal deliveryScore;
    
    /** 质量评分 */
    private BigDecimal qualityScore;
    
    /** 服务评分 */
    private BigDecimal serviceScore;
    
    /** 准时率 */
    private BigDecimal onTimeRate;
    
    /** 合格率 */
    private BigDecimal qualityRate;
    
    /** 状态 1-启用 0-停用 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
}
