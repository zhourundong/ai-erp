package com.aierp.mapper;

import com.aierp.entity.PurchaseOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    @Select("SELECT * FROM purchase_orders WHERE order_no = #{orderNo} AND deleted = 0")
    PurchaseOrder findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM purchase_orders WHERE supplier_id = #{supplierId} AND deleted = 0 ORDER BY created_at DESC")
    List<PurchaseOrder> findBySupplierId(@Param("supplierId") Long supplierId);

    @Select("SELECT * FROM purchase_orders WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<PurchaseOrder> findByStatus(@Param("status") String status);
}
