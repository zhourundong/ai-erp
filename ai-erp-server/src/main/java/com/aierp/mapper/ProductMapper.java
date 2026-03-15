package com.aierp.mapper;

import com.aierp.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM products WHERE sku = #{sku} AND deleted = 0")
    Product findBySku(@Param("sku") String sku);

    @Select("SELECT * FROM products WHERE category_id = #{categoryId} AND deleted = 0 ORDER BY created_at DESC")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT * FROM products WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY created_at DESC")
    List<Product> findActiveProducts();
}
