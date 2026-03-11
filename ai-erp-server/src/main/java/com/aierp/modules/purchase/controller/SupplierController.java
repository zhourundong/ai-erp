package com.aierp.modules.purchase.controller;

import com.aierp.common.result.Result;
import com.aierp.modules.purchase.entity.Supplier;
import com.aierp.modules.purchase.service.SupplierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商管理接口
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    
    private final SupplierService supplierService;
    
    @GetMapping
    public Result<Page<Supplier>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<Supplier> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Supplier::getSupplierName, keyword)
                   .or()
                   .like(Supplier::getSupplierCode, keyword);
        }
        
        wrapper.orderByDesc(Supplier::getCreatedTime);
        return Result.success(supplierService.page(pageParam, wrapper));
    }
    
    @PostMapping
    public Result<Long> create(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.success(supplier.getId());
    }
    
    @GetMapping("/{id}")
    public Result<Supplier> get(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierService.updateById(supplier);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.removeById(id);
        return Result.success();
    }
}
