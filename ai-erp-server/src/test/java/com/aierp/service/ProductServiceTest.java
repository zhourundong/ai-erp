package com.aierp.service;

import com.aierp.entity.Product;
import com.aierp.mapper.ProductMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 商品服务测试
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("SKU001");
        testProduct.setName("测试商品");
        testProduct.setCategoryName("办公用品");
        testProduct.setUnit("个");
        testProduct.setCostPrice(new BigDecimal("10.00"));
        testProduct.setSalePrice(new BigDecimal("15.00"));
        testProduct.setStatus("ACTIVE");
    }

    @Test
    void testCreateProduct_Success() {
        // Given
        Product newProduct = new Product();
        newProduct.setName("新商品");
        newProduct.setUnit("个");

        when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return 1;
        });

        // When
        Product created = productService.createProduct(newProduct);

        // Then
        assertNotNull(created);
        assertNotNull(created.getSku());
        assertEquals("ACTIVE", created.getStatus());
        verify(productMapper).insert(any(Product.class));
    }

    @Test
    void testPageProducts() {
        // Given
        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testProduct));
        mockPage.setTotal(1);

        when(productMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<Product> result = productService.pageProducts(1, 10, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testGetBySku() {
        // Given
        when(productMapper.findBySku("SKU001")).thenReturn(testProduct);

        // When
        Product found = productService.getBySku("SKU001");

        // Then
        assertNotNull(found);
        assertEquals("SKU001", found.getSku());
    }

    @Test
    void testUpdateProduct() {
        // Given
        testProduct.setName("更新后的商品");
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When
        Product updated = productService.updateProduct(testProduct);

        // Then
        assertEquals("更新后的商品", updated.getName());
        verify(productMapper).updateById(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        // Given
        when(productMapper.deleteById(1L)).thenReturn(1);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productMapper).deleteById(1L);
    }
}
