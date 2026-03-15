package com.aierp.mapper;

import com.aierp.entity.SalesOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SalesOrderItemMapper extends BaseMapper<SalesOrderItem> {

    @Select("SELECT * FROM sales_order_items WHERE order_id = #{orderId} AND deleted = 0 ORDER BY id")
    List<SalesOrderItem> findByOrderId(@Param("orderId") Long orderId);
}
