package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织实体
 */
@Data
@TableName("organizations")
public class Organization {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 组织名称 */
    private String name;

    /** 组织编码 */
    private String code;

    /** 父组织ID */
    private Long parentId;

    /** 组织类型：COMPANY-公司, BRANCH-分公司, DEPARTMENT-部门 */
    private String type;

    /** 状态：ACTIVE-启用, INACTIVE-停用 */
    private String status;

    /** 描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
