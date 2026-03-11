package com.aierp.modules.purchase.controller;

import com.aierp.common.result.Result;
import com.aierp.modules.purchase.entity.Product;
import com.aierp.modules.purchase.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理接口
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getProductName, keyword)
                   .or()
                   .like(Product::getProductCode, keyword);
        }
        
        wrapper.orderByDesc(Product::getCreatedTime);
        return Result.success(productService.page(pageParam, wrapper));
    }
    
    @PostMapping
    public Result<Long> create(@RequestBody Product product) {
        productService.save(product);
        return Result.success(product.getId());
    }
    
    @GetMapping("/{id}")
    public Result<Product> get(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.updateById(product);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
