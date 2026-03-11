package com.aierp.modules.purchase.controller;

import com.aierp.common.result.Result;
import com.aierp.modules.purchase.entity.Inventory;
import com.aierp.modules.purchase.service.InventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理接口
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping
    public Result<Page<Inventory>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer warningStatus) {
        
        Page<Inventory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        
        if (productId != null) {
            wrapper.eq(Inventory::getProductId, productId);
        }
        
        if (warningStatus != null) {
            wrapper.eq(Inventory::getAiWarningStatus, warningStatus);
        }
        
        return Result.success(inventoryService.page(pageParam, wrapper));
    }
    
    @GetMapping("/{id}")
    public Result<Inventory> get(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }
    
    @GetMapping("/warnings")
    public Result<Page<Inventory>> warnings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<Inventory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getAiWarningStatus, 1);
        
        return Result.success(inventoryService.page(pageParam, wrapper));
    }
}
