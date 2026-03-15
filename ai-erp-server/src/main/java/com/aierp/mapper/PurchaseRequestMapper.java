package com.aierp.mapper;

import com.aierp.entity.PurchaseRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购申请Mapper接口
 */
@Mapper
public interface PurchaseRequestMapper extends BaseMapper<PurchaseRequest> {

    @Select("SELECT * FROM purchase_requests WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<PurchaseRequest> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM purchase_requests WHERE applicant_id = #{applicantId} AND deleted = 0 ORDER BY created_at DESC")
    List<PurchaseRequest> findByApplicantId(@Param("applicantId") Long applicantId);

    @Select("SELECT * FROM purchase_requests WHERE request_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY request_date DESC")
    List<PurchaseRequest> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM purchase_requests WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") String status);
}
