package com.aierp.mapper;

import com.aierp.entity.PurchaseRequestItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 采购申请明细Mapper接口
 */
@Mapper
public interface PurchaseRequestItemMapper extends BaseMapper<PurchaseRequestItem> {

    @Select("SELECT * FROM purchase_request_items WHERE request_id = #{requestId} AND deleted = 0")
    List<PurchaseRequestItem> findByRequestId(@Param("requestId") Long requestId);
}
