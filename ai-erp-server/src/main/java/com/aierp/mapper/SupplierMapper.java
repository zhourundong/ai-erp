package com.aierp.mapper;

import com.aierp.entity.Supplier;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商Mapper接口
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {

    @Select("SELECT * FROM suppliers WHERE score >= #{minScore} AND status = 'ACTIVE' AND deleted = 0 ORDER BY score DESC")
    List<Supplier> findTopSuppliers(@Param("minScore") BigDecimal minScore);

    @Select("SELECT * FROM suppliers WHERE category LIKE '%' || #{category} || '%' AND status = 'ACTIVE' AND deleted = 0")
    List<Supplier> findByCategory(@Param("category") String category);

    @Select("SELECT COUNT(*) FROM suppliers WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") String status);

    @Select("SELECT * FROM suppliers WHERE code = #{code} AND deleted = 0")
    Supplier findByCode(@Param("code") String code);
}
