package com.aierp.mapper;

import com.aierp.entity.Inventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("SELECT * FROM inventories WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} AND deleted = 0")
    Inventory findByWarehouseAndProduct(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId);

    @Select("SELECT * FROM inventories WHERE warehouse_id = #{warehouseId} AND deleted = 0")
    List<Inventory> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Select("SELECT * FROM inventories WHERE product_id = #{productId} AND deleted = 0")
    List<Inventory> findByProductId(@Param("productId") Long productId);

    @Update("UPDATE inventories SET quantity = quantity + #{delta}, available_qty = available_qty + #{delta}, updated_at = datetime('now') WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} AND deleted = 0")
    int updateQuantity(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId, @Param("delta") BigDecimal delta);
}
