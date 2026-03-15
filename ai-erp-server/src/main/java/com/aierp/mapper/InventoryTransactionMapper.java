package com.aierp.mapper;

import com.aierp.entity.InventoryTransaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {

    @Select("SELECT * FROM inventory_transactions WHERE transaction_no = #{transactionNo}")
    InventoryTransaction findByTransactionNo(@Param("transactionNo") String transactionNo);

    @Select("SELECT * FROM inventory_transactions WHERE product_id = #{productId} ORDER BY created_at DESC LIMIT #{limit}")
    List<InventoryTransaction> findByProductId(@Param("productId") Long productId, @Param("limit") int limit);

    @Select("SELECT * FROM inventory_transactions WHERE warehouse_id = #{warehouseId} ORDER BY created_at DESC LIMIT #{limit}")
    List<InventoryTransaction> findByWarehouseId(@Param("warehouseId") Long warehouseId, @Param("limit") int limit);
}
