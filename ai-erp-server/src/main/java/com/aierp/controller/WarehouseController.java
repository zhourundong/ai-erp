package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.Warehouse;
import com.aierp.service.WarehouseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public Result<Page<Warehouse>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(warehouseService.pageWarehouses(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Warehouse> get(@PathVariable Long id) {
        return Result.success(warehouseService.getById(id));
    }

    @PostMapping
    public Result<Warehouse> create(@RequestBody Warehouse warehouse) {
        return Result.success(warehouseService.createWarehouse(warehouse));
    }

    @PutMapping("/{id}")
    public Result<Warehouse> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        return Result.success(warehouseService.updateWarehouse(warehouse));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success();
    }
}
