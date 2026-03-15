package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.Inventory;
import com.aierp.service.InventoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<Page<Inventory>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId) {
        return Result.success(inventoryService.pageInventory(pageNum, pageSize, warehouseId, productId));
    }

    @GetMapping("/{warehouseId}/{productId}")
    public Result<Inventory> get(@PathVariable Long warehouseId, @PathVariable Long productId) {
        return Result.success(inventoryService.getByWarehouseAndProduct(warehouseId, productId));
    }

    @PostMapping("/stock-in")
    public Result<Inventory> stockIn(@RequestBody Map<String, Object> params) {
        Long warehouseId = Long.valueOf(params.get("warehouseId").toString());
        Long productId = Long.valueOf(params.get("productId").toString());
        String productSku = (String) params.get("productSku");
        String productName = (String) params.get("productName");
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
        BigDecimal costPrice = params.get("costPrice") != null ? new BigDecimal(params.get("costPrice").toString()) : BigDecimal.ZERO;
        String orderType = (String) params.get("orderType");
        Long orderId = params.get("orderId") != null ? Long.valueOf(params.get("orderId").toString()) : null;
        String orderNo = (String) params.get("orderNo");
        String operator = (String) params.get("operator");

        return Result.success(inventoryService.stockIn(warehouseId, productId, productSku, productName,
                quantity, costPrice, orderType, orderId, orderNo, operator));
    }

    @PostMapping("/stock-out")
    public Result<Inventory> stockOut(@RequestBody Map<String, Object> params) {
        Long warehouseId = Long.valueOf(params.get("warehouseId").toString());
        Long productId = Long.valueOf(params.get("productId").toString());
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
        String orderType = (String) params.get("orderType");
        Long orderId = params.get("orderId") != null ? Long.valueOf(params.get("orderId").toString()) : null;
        String orderNo = (String) params.get("orderNo");
        String operator = (String) params.get("operator");

        return Result.success(inventoryService.stockOut(warehouseId, productId, quantity, orderType, orderId, orderNo, operator));
    }
}
