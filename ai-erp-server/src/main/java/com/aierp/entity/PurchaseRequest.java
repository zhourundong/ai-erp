package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购申请实体
 */
@Data
@TableName("purchase_requests")
public class PurchaseRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请单号 */
    private String requestNo;

    /** 申请日期 */
    private LocalDate requestDate;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 部门ID */
    private Long departmentId;

    /** 部门名称 */
    private String departmentName;

    /** 自然语言需求描述 */
    private String requirementDescription;

    /** AI解析结果（JSON格式） */
    private String aiParsedResult;

    /** AI推荐的采购方案（JSON格式） */
    private String aiRecommendation;

    /** AI预估总金额 */
    private BigDecimal aiEstimatedAmount;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 状态：DRAFT-草稿, PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝 */
    private String status;

    /** 优先级：HIGH-高, NORMAL-普通, LOW-低 */
    private String priority;

    /** 审批人ID */
    private Long approverId;

    /** 审批人姓名 */
    private String approverName;

    /** 审批时间 */
    private LocalDateTime approvalTime;

    /** 审批意见 */
    private String approvalComment;

    /** 关联的采购订单ID */
    private Long purchaseOrderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
