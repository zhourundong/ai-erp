package com.aierp.mapper;

import com.aierp.entity.Customer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    @Select("SELECT * FROM customers WHERE code = #{code} AND deleted = 0")
    Customer findByCode(@Param("code") String code);

    @Select("SELECT * FROM customers WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY created_at DESC")
    List<Customer> findActiveCustomers();
}
