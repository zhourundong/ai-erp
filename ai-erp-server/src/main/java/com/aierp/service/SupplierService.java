package com.aierp.service;

import com.aierp.entity.Supplier;
import com.aierp.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商服务
 */
@Service
@RequiredArgsConstructor
public class SupplierService extends ServiceImpl<SupplierMapper, Supplier> {

    private final SupplierMapper supplierMapper;

    /**
     * 分页查询供应商
     */
    public Page<Supplier> pageSuppliers(int pageNum, int pageSize, String keyword, String status) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Supplier::getName, keyword)
                    .or().like(Supplier::getCode, keyword)
                    .or().like(Supplier::getContactPerson, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Supplier::getStatus, status);
        }
        wrapper.orderByDesc(Supplier::getCreatedAt);
        return supplierMapper.selectPage(page, wrapper);
    }

    /**
     * 获取供应商详情
     */
    public Supplier getSupplierById(Long id) {
        return supplierMapper.selectById(id);
    }

    /**
     * 创建供应商
     */
    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        if (supplier.getCode() == null || supplier.getCode().isEmpty()) {
            supplier.setCode("SUP" + System.currentTimeMillis());
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus("ACTIVE");
        }
        if (supplier.getTransactionCount() == null) {
            supplier.setTransactionCount(0);
        }
        if (supplier.getTotalTransactionAmount() == null) {
            supplier.setTotalTransactionAmount(BigDecimal.ZERO);
        }
        if (supplier.getScore() == null) {
            supplier.setScore(BigDecimal.ZERO);
        }
        supplierMapper.insert(supplier);
        return supplier;
    }

    /**
     * 更新供应商
     */
    @Transactional
    public Supplier updateSupplier(Supplier supplier) {
        supplierMapper.updateById(supplier);
        return supplier;
    }

    /**
     * 删除供应商
     */
    @Transactional
    public void deleteSupplier(Long id) {
        supplierMapper.deleteById(id);
    }

    /**
     * AI推荐供应商
     */
    public List<Supplier> recommendSuppliers(String category, BigDecimal minScore) {
        if (minScore == null) {
            minScore = new BigDecimal("70");
        }
        if (category != null && !category.isEmpty()) {
            return supplierMapper.findByCategory(category);
        }
        return supplierMapper.findTopSuppliers(minScore);
    }
}
