package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库实体
 */
@Data
@TableName("warehouses")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 仓库编码 */
    private String code;

    /** 仓库名称 */
    private String name;

    /** 仓库类型 */
    private String type;

    /** 地址 */
    private String address;

    /** 负责人 */
    private String manager;

    /** 联系电话 */
    private String phone;

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
