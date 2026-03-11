package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long tenantId;
    private String roleName;
    private String roleCode;
    private String description;
    private String dataScope;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableLogic
    private Integer deleted;
}
