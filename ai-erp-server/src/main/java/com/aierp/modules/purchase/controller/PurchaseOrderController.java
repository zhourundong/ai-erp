package com.aierp.modules.purchase.controller;

import com.aierp.common.result.Result;
import com.aierp.modules.purchase.entity.PurchaseOrder;
import com.aierp.modules.purchase.service.PurchaseOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 采购订单管理接口
 */
@RestController
@RequestMapping("/api/v1/purchase/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    private final PurchaseOrderService purchaseOrderService;
    
    @GetMapping
    public Result<Page<PurchaseOrder>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status) {
        
        Page<PurchaseOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        
        wrapper.orderByDesc(PurchaseOrder::getCreatedTime);
        return Result.success(purchaseOrderService.page(pageParam, wrapper));
    }
    
    @PostMapping
    public Result<Long> create(@RequestBody PurchaseOrder order) {
        purchaseOrderService.save(order);
        return Result.success(order.getId());
    }
    
    @GetMapping("/{id}")
    public Result<PurchaseOrder> get(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getById(id));
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PurchaseOrder order) {
        order.setId(id);
        purchaseOrderService.updateById(order);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.removeById(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getById(id);
        order.setStatus(1); // 待审批
        purchaseOrderService.updateById(order);
        return Result.success();
    }
    
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getById(id);
        order.setStatus(2); // 已审批
        purchaseOrderService.updateById(order);
        return Result.success();
    }
}
