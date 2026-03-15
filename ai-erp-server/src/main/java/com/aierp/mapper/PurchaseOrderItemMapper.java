package com.aierp.mapper;

import com.aierp.entity.PurchaseOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {

    @Select("SELECT * FROM purchase_order_items WHERE order_id = #{orderId} AND deleted = 0 ORDER BY id")
    List<PurchaseOrderItem> findByOrderId(@Param("orderId") Long orderId);
}
