package com.aierp.service;

import com.aierp.entity.Warehouse;
import com.aierp.mapper.WarehouseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService extends ServiceImpl<WarehouseMapper, Warehouse> {

    private final WarehouseMapper warehouseMapper;

    public Page<Warehouse> pageWarehouses(int pageNum, int pageSize, String keyword, String status) {
        Page<Warehouse> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Warehouse::getName, keyword)
                    .or().like(Warehouse::getCode, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        wrapper.orderByDesc(Warehouse::getCreatedAt);
        return warehouseMapper.selectPage(page, wrapper);
    }

    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        if (warehouse.getCode() == null || warehouse.getCode().isEmpty()) {
            warehouse.setCode("WH" + System.currentTimeMillis());
        }
        if (warehouse.getStatus() == null) {
            warehouse.setStatus("ACTIVE");
        }
        warehouseMapper.insert(warehouse);
        return warehouse;
    }

    @Transactional
    public Warehouse updateWarehouse(Warehouse warehouse) {
        warehouseMapper.updateById(warehouse);
        return warehouse;
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        warehouseMapper.deleteById(id);
    }
}
