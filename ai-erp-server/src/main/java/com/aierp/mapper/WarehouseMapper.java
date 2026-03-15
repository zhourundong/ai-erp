package com.aierp.mapper;

import com.aierp.entity.Warehouse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    @Select("SELECT * FROM warehouses WHERE code = #{code} AND deleted = 0")
    Warehouse findByCode(@Param("code") String code);

    @Select("SELECT * FROM warehouses WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY created_at DESC")
    List<Warehouse> findActiveWarehouses();
}
