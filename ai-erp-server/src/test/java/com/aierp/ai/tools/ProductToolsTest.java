package com.aierp.ai.tools;

import com.aierp.entity.Product;
import com.aierp.service.ProductService;
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
 * 商品工具测试
 */
@ExtendWith(MockitoExtension.class)
class ProductToolsTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductTools productTools;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("SKU001");
        testProduct.setName("测试商品");
        testProduct.setCategoryName("办公用品");
        testProduct.setBrand("测试品牌");
        testProduct.setUnit("个");
        testProduct.setSalePrice(BigDecimal.valueOf(100));
        testProduct.setStatus("ACTIVE");
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        // Given
        when(productService.getBySku("SKU002")).thenReturn(null);
        when(productService.createProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        // When
        String result = productTools.createProduct(
                "SKU002",
                "新商品",
                "电子设备",
                "品牌",
                "型号",
                "规格",
                "件",
                50.0,
                100.0,
                120.0,
                10,
                5,
                "描述"
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"id\":2"));
        assertTrue(result.contains("\"sku\":\"SKU002\""));
        assertTrue(result.contains("\"name\":\"新商品\""));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void testCreateProduct_EmptySku_AutoGenerate() throws Exception {
        // Given - 空SKU时由Service层自动生成
        when(productService.createProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(3L);
            p.setSku("SKU" + System.currentTimeMillis());
            return p;
        });

        // When
        String result = productTools.createProduct(
                "",
                "商品名",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"id\":3"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void testCreateProduct_NullSku_AutoGenerate() throws Exception {
        // Given - SKU为null时由Service层自动生成
        when(productService.createProduct(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(4L);
            p.setSku("SKU" + System.currentTimeMillis());
            return p;
        });

        // When
        String result = productTools.createProduct(
                null,
                "商品名",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"id\":4"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void testCreateProduct_EmptyName() throws Exception {
        // When
        String result = productTools.createProduct(
                "SKU003",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("商品名称不能为空"));

        verify(productService, never()).createProduct(any());
    }

    @Test
    void testCreateProduct_SkuExists() throws Exception {
        // Given
        when(productService.getBySku("SKU001")).thenReturn(testProduct);

        // When
        String result = productTools.createProduct(
                "SKU001",
                "新商品",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("商品SKU已存在"));

        verify(productService, never()).createProduct(any());
    }

    @Test
    void testSearchProducts_Success() throws Exception {
        // Given
        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testProduct));
        mockPage.setTotal(1);

        when(productService.pageProducts(1, 10, "测试", "ACTIVE", null)).thenReturn(mockPage);

        // When
        String result = productTools.searchProducts("测试", 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"name\":\"测试商品\""));
    }

    @Test
    void testSearchProducts_DefaultPaging() throws Exception {
        // Given
        Page<Product> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(productService.pageProducts(1, 10, null, "ACTIVE", null)).thenReturn(mockPage);

        // When
        String result = productTools.searchProducts(null, null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
    }

    @Test
    void testGetProductBySku_Success() throws Exception {
        // Given
        when(productService.getBySku("SKU001")).thenReturn(testProduct);

        // When
        String result = productTools.getProductBySku("SKU001");

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"name\":\"测试商品\""));
        assertTrue(result.contains("\"sku\":\"SKU001\""));
    }

    @Test
    void testGetProductBySku_NotFound() throws Exception {
        // Given
        when(productService.getBySku("NOTEXIST")).thenReturn(null);

        // When
        String result = productTools.getProductBySku("NOTEXIST");

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("商品不存在"));
    }

    @Test
    void testGetProductById_Success() throws Exception {
        // Given
        when(productService.getById(1L)).thenReturn(testProduct);

        // When
        String result = productTools.getProductById(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"name\":\"测试商品\""));
        assertTrue(result.contains("\"id\":1"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        // Given
        when(productService.getById(999L)).thenReturn(null);

        // When
        String result = productTools.getProductById(999L);

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("商品不存在"));
    }
}
