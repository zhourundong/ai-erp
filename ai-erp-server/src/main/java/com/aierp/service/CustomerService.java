package com.aierp.service;

import com.aierp.entity.Customer;
import com.aierp.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {

    private final CustomerMapper customerMapper;

    public Page<Customer> pageCustomers(int pageNum, int pageSize, String keyword, String status) {
        Page<Customer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or().like(Customer::getCode, keyword)
                    .or().like(Customer::getContactPerson, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Customer::getStatus, status);
        }
        wrapper.orderByDesc(Customer::getCreatedAt);
        return customerMapper.selectPage(page, wrapper);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer.getCode() == null || customer.getCode().isEmpty()) {
            customer.setCode("CUS" + System.currentTimeMillis());
        }
        if (customer.getStatus() == null) {
            customer.setStatus("ACTIVE");
        }
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(BigDecimal.ZERO);
        }
        if (customer.getCreditUsed() == null) {
            customer.setCreditUsed(BigDecimal.ZERO);
        }
        customerMapper.insert(customer);
        return customer;
    }

    @Transactional
    public Customer updateCustomer(Customer customer) {
        customerMapper.updateById(customer);
        return customer;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        customerMapper.deleteById(id);
    }
}
