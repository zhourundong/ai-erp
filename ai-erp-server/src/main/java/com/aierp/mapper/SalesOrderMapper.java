package com.aierp.mapper;

import com.aierp.entity.SalesOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {

    @Select("SELECT * FROM sales_orders WHERE order_no = #{orderNo} AND deleted = 0")
    SalesOrder findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM sales_orders WHERE customer_id = #{customerId} AND deleted = 0 ORDER BY created_at DESC")
    List<SalesOrder> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM sales_orders WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<SalesOrder> findByStatus(@Param("status") String status);
}
