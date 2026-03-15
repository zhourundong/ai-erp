package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.Product;
import com.aierp.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(productService.pageProducts(pageNum, pageSize, keyword, status, categoryId));
    }

    @GetMapping("/{id}")
    public Result<Product> get(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return Result.success(productService.updateProduct(product));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
