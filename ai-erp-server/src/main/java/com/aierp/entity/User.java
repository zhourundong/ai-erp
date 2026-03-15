package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 组织ID */
    private Long organizationId;

    /** 组织名称 */
    @TableField(exist = false)
    private String organizationName;

    /** 角色：ADMIN-管理员, BUYER-采购员, APPROVER-审批人, VIEWER-查看者 */
    private String role;

    /** 状态：ACTIVE-启用, INACTIVE-停用 */
    private String status;

    /** 头像URL */
    private String avatar;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
