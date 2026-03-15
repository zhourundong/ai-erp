package com.aierp.service;

import com.aierp.entity.Product;
import com.aierp.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final ProductMapper productMapper;

    public Page<Product> pageProducts(int pageNum, int pageSize, String keyword, String status, Long categoryId) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Product::getName, keyword)
                    .or().like(Product::getSku, keyword)
                    .or().like(Product::getBrand, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Product::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(page, wrapper);
    }

    public Product getBySku(String sku) {
        return productMapper.findBySku(sku);
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku("SKU" + System.currentTimeMillis());
        }
        if (product.getStatus() == null) {
            product.setStatus("ACTIVE");
        }
        if (product.getCostPrice() == null) {
            product.setCostPrice(BigDecimal.ZERO);
        }
        if (product.getSalePrice() == null) {
            product.setSalePrice(BigDecimal.ZERO);
        }
        productMapper.insert(product);
        return product;
    }

    @Transactional
    public Product updateProduct(Product product) {
        productMapper.updateById(product);
        return product;
    }

    @Transactional
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
