package com.aierp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aierp.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    @Select("SELECT r.* FROM sys_role r INNER JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
