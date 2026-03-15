package com.aierp.mapper;

import com.aierp.entity.Organization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织Mapper接口
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

    @Select("SELECT * FROM organizations WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY id")
    List<Organization> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM organizations WHERE type = #{type} AND deleted = 0 ORDER BY id")
    List<Organization> findByType(@Param("type") String type);
}
