package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@TableName("customers")
public class Customer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户编码 */
    private String code;

    /** 客户名称 */
    private String name;

    /** 简称 */
    private String shortName;

    /** 客户等级 */
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

    /** 信用额度 */
    private BigDecimal creditLimit;

    /** 已用信用额度 */
    private BigDecimal creditUsed;

    /** 评级 */
    private Integer rating;

    /** 状态 */
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
