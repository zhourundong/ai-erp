package com.aierp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;
    private String menuName;
    private String menuCode;
    private Integer menuType;  // 1-目录 2-菜单 3-按钮
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
