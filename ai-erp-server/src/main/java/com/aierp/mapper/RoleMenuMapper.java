package com.aierp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aierp.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 " +
            "ORDER BY m.sort")
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);
}
