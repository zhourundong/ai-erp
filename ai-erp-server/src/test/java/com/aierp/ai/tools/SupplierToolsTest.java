package com.aierp.ai.tools;

import com.aierp.entity.Supplier;
import com.aierp.service.SupplierService;
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
 * 供应商工具测试
 */
@ExtendWith(MockitoExtension.class)
class SupplierToolsTest {

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SupplierTools supplierTools;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setCode("SUP001");
        testSupplier.setName("测试供应商");
        testSupplier.setCategory("办公用品");
        testSupplier.setContactPerson("张三");
        testSupplier.setContactPhone("13800138000");
        testSupplier.setScore(new BigDecimal("85"));
        testSupplier.setStatus("ACTIVE");
    }

    @Test
    void testCreateSupplier_Success() throws Exception {
        // Given
        when(supplierService.createSupplier(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setId(2L);
            s.setCode("SUP002");
            return s;
        });

        // When
        String result = supplierTools.createSupplier(
                "新供应商",
                "简称",
                "电子设备",
                "李四",
                "13900139000",
                "test@example.com",
                "上海市",
                null, null, null,
                "备注"
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"id\":2"));
        assertTrue(result.contains("\"name\":\"新供应商\""));

        verify(supplierService).createSupplier(any(Supplier.class));
    }

    @Test
    void testCreateSupplier_EmptyName() throws Exception {
        // When
        String result = supplierTools.createSupplier(
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null, null, null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("供应商名称不能为空"));

        verify(supplierService, never()).createSupplier(any());
    }

    @Test
    void testCreateSupplier_NullName() throws Exception {
        // When
        String result = supplierTools.createSupplier(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null, null, null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("供应商名称不能为空"));
    }

    @Test
    void testSearchSuppliers_Success() throws Exception {
        // Given
        Page<Supplier> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testSupplier));
        mockPage.setTotal(1);

        when(supplierService.pageSuppliers(1, 10, "测试", "ACTIVE")).thenReturn(mockPage);

        // When
        String result = supplierTools.searchSuppliers("测试", 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"name\":\"测试供应商\""));
    }

    @Test
    void testSearchSuppliers_DefaultPaging() throws Exception {
        // Given
        Page<Supplier> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(supplierService.pageSuppliers(1, 10, null, "ACTIVE")).thenReturn(mockPage);

        // When
        String result = supplierTools.searchSuppliers(null, null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
    }

    @Test
    void testRecommendSuppliers_Success() throws Exception {
        // Given
        when(supplierService.recommendSuppliers(any(), any(BigDecimal.class)))
                .thenReturn(List.of(testSupplier));

        // When
        String result = supplierTools.recommendSuppliers("办公用品", 70.0);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"name\":\"测试供应商\""));
    }

    @Test
    void testRecommendSuppliers_EmptyResult() throws Exception {
        // Given
        when(supplierService.recommendSuppliers(any(), any(BigDecimal.class)))
                .thenReturn(List.of());

        // When
        String result = supplierTools.recommendSuppliers(null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
        assertTrue(result.contains("未找到符合条件的供应商"));
    }

    @Test
    void testGetSupplierDetail_Success() throws Exception {
        // Given
        when(supplierService.getSupplierById(1L)).thenReturn(testSupplier);

        // When
        String result = supplierTools.getSupplierDetail(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"name\":\"测试供应商\""));
        assertTrue(result.contains("\"contactPerson\":\"张三\""));
    }

    @Test
    void testGetSupplierDetail_NotFound() throws Exception {
        // Given
        when(supplierService.getSupplierById(999L)).thenReturn(null);

        // When
        String result = supplierTools.getSupplierDetail(999L);

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("供应商不存在"));
    }
}
