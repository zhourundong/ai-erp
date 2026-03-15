package com.aierp.mapper;

import com.aierp.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.*, o.name as organization_name FROM users u " +
            "LEFT JOIN organizations o ON u.organization_id = o.id " +
            "WHERE u.username = #{username} AND u.deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT u.*, o.name as organization_name FROM users u " +
            "LEFT JOIN organizations o ON u.organization_id = o.id " +
            "WHERE u.id = #{id} AND u.deleted = 0")
    User findByIdWithOrg(@Param("id") Long id);

    @Select("SELECT u.*, o.name as organization_name FROM users u " +
            "LEFT JOIN organizations o ON u.organization_id = o.id " +
            "WHERE u.organization_id = #{orgId} AND u.deleted = 0 " +
            "ORDER BY u.created_at DESC")
    List<User> findByOrganizationId(@Param("orgId") Long orgId);

    @Select("SELECT COUNT(*) FROM users WHERE deleted = 0")
    long countAll();

    @Select("SELECT COUNT(*) FROM users WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") String status);
}
