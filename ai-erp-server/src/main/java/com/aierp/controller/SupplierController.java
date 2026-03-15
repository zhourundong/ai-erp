package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.Supplier;
import com.aierp.service.SupplierService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商管理控制器
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * 分页查询供应商
     */
    @GetMapping
    public ResponseEntity<Result<Page<Supplier>>> pageSuppliers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<Supplier> page = supplierService.pageSuppliers(pageNum, pageSize, keyword, status);
        return ResponseEntity.ok(Result.success(page));
    }

    /**
     * 获取供应商详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Supplier>> getSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(Result.success(supplier));
    }

    /**
     * 创建供应商
     */
    @PostMapping
    public ResponseEntity<Result<Supplier>> createSupplier(@RequestBody Supplier supplier) {
        Supplier created = supplierService.createSupplier(supplier);
        return ResponseEntity.ok(Result.success(created));
    }

    /**
     * 更新供应商
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Supplier>> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier supplier) {
        supplier.setId(id);
        Supplier updated = supplierService.updateSupplier(supplier);
        return ResponseEntity.ok(Result.success(updated));
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(Result.success());
    }

    /**
     * AI推荐供应商
     */
    @PostMapping("/recommend")
    public ResponseEntity<Result<List<Supplier>>> recommendSuppliers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minScore) {
        List<Supplier> suppliers = supplierService.recommendSuppliers(category, minScore);
        return ResponseEntity.ok(Result.success(suppliers));
    }
}
