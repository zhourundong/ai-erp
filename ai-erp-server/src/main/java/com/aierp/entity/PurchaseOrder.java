package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购订单实体
 */
@Data
@TableName("purchase_orders")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 订单日期 */
    private LocalDate orderDate;

    /** 关联采购申请ID */
    private Long requestId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 应付金额 */
    private BigDecimal payAmount;

    /** 状态：DRAFT-草稿, PENDING-待审批, APPROVED-已审批, RECEIVING-收货中, COMPLETED-已完成, CANCELLED-已取消 */
    private String status;

    /** 付款状态：UNPAID-未付款, PARTIAL-部分付款, PAID-已付款 */
    private String paymentStatus;

    /** 付款方式 */
    private String paymentMethod;

    /** 预计交货日期 */
    private LocalDate expectedDeliveryDate;

    /** 实际交货日期 */
    private LocalDate actualDeliveryDate;

    /** 采购员ID */
    private Long buyerId;

    /** 采购员姓名 */
    private String buyerName;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
