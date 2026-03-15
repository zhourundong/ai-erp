package com.aierp.service;

import com.aierp.entity.Warehouse;
import com.aierp.mapper.WarehouseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 仓库服务测试
 */
@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setCode("WH001");
        testWarehouse.setName("主仓库");
        testWarehouse.setType("MAIN");
        testWarehouse.setManager("王五");
        testWarehouse.setPhone("13900139000");
        testWarehouse.setStatus("ACTIVE");
    }

    @Test
    void testCreateWarehouse_Success() {
        // Given
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setName("新仓库");

        when(warehouseMapper.insert(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse w = invocation.getArgument(0);
            w.setId(2L);
            return 1;
        });

        // When
        Warehouse created = warehouseService.createWarehouse(newWarehouse);

        // Then
        assertNotNull(created);
        assertNotNull(created.getCode());
        assertEquals("ACTIVE", created.getStatus());
        verify(warehouseMapper).insert(any(Warehouse.class));
    }

    @Test
    void testPageWarehouses() {
        // Given
        Page<Warehouse> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testWarehouse));
        mockPage.setTotal(1);

        when(warehouseMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<Warehouse> result = warehouseService.pageWarehouses(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testUpdateWarehouse() {
        // Given
        testWarehouse.setName("更新后的仓库");
        when(warehouseMapper.updateById(any(Warehouse.class))).thenReturn(1);

        // When
        Warehouse updated = warehouseService.updateWarehouse(testWarehouse);

        // Then
        assertEquals("更新后的仓库", updated.getName());
        verify(warehouseMapper).updateById(any(Warehouse.class));
    }

    @Test
    void testDeleteWarehouse() {
        // Given
        when(warehouseMapper.deleteById(1L)).thenReturn(1);

        // When
        warehouseService.deleteWarehouse(1L);

        // Then
        verify(warehouseMapper).deleteById(1L);
    }
}
