package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单实体
 */
@Data
@TableName("sales_orders")
public class SalesOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 订单日期 */
    private LocalDate orderDate;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 应收金额 */
    private BigDecimal payAmount;

    /** 状态 */
    private String status;

    /** 收款状态 */
    private String paymentStatus;

    /** 收款方式 */
    private String paymentMethod;

    /** 预计发货日期 */
    private LocalDate expectedDeliveryDate;

    /** 实际发货日期 */
    private LocalDate actualDeliveryDate;

    /** 销售员ID */
    private Long salesId;

    /** 销售员姓名 */
    private String salesName;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
