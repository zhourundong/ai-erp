package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
@TableName("suppliers")
public class Supplier {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商编码 */
    private String code;

    /** 供应商名称 */
    private String name;

    /** 简称 */
    private String shortName;

    /** 供应商类别 */
    private String category;

    /** 供应商等级 */
    private String level;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 邮箱 */
    private String contactEmail;

    /** 地址 */
    private String address;

    /** 开户行 */
    private String bankName;

    /** 银行账户 */
    private String bankAccount;

    /** 税号 */
    private String taxNumber;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 评级 (1-5) */
    private Integer rating;

    /** AI综合评分 */
    private BigDecimal score;

    /** 历史交易次数 */
    private Integer transactionCount;

    /** 历史交易总额 */
    private BigDecimal totalTransactionAmount;

    /** 状态：ACTIVE-启用, INACTIVE-停用 */
    private String status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
