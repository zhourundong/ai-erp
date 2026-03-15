package com.aierp.service;

import com.aierp.entity.Supplier;
import com.aierp.mapper.SupplierMapper;
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
 * 供应商服务测试
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setCode("SUP001");
        testSupplier.setName("测试供应商");
        testSupplier.setContactPerson("张三");
        testSupplier.setContactPhone("13800138000");
        testSupplier.setStatus("ACTIVE");
        testSupplier.setScore(new BigDecimal("85"));
    }

    @Test
    void testCreateSupplier_Success() {
        // Given
        Supplier newSupplier = new Supplier();
        newSupplier.setName("新供应商");

        when(supplierMapper.insert(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setId(2L);
            return 1;
        });

        // When
        Supplier created = supplierService.createSupplier(newSupplier);

        // Then
        assertNotNull(created);
        assertNotNull(created.getCode());
        assertEquals("ACTIVE", created.getStatus());
        assertEquals(0, created.getTransactionCount());

        verify(supplierMapper).insert(any(Supplier.class));
    }

    @Test
    void testPageSuppliers() {
        // Given
        Page<Supplier> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testSupplier));
        mockPage.setTotal(1);

        when(supplierMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<Supplier> result = supplierService.pageSuppliers(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testGetSupplierById() {
        // Given
        when(supplierMapper.selectById(1L)).thenReturn(testSupplier);

        // When
        Supplier found = supplierService.getSupplierById(1L);

        // Then
        assertNotNull(found);
        assertEquals("SUP001", found.getCode());
    }

    @Test
    void testDeleteSupplier() {
        // Given
        when(supplierMapper.deleteById(1L)).thenReturn(1);

        // When
        supplierService.deleteSupplier(1L);

        // Then
        verify(supplierMapper).deleteById(1L);
    }

    @Test
    void testRecommendSuppliers() {
        // Given
        when(supplierMapper.findTopSuppliers(any(BigDecimal.class)))
                .thenReturn(List.of(testSupplier));

        // When
        List<Supplier> result = supplierService.recommendSuppliers(null, new BigDecimal("70"));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getScore().compareTo(new BigDecimal("70")) >= 0);
    }
}
