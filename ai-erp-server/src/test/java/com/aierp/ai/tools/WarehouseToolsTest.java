package com.aierp.ai.tools;

import com.aierp.entity.Warehouse;
import com.aierp.service.WarehouseService;
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
 * 仓库工具测试
 */
@ExtendWith(MockitoExtension.class)
class WarehouseToolsTest {

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseTools warehouseTools;

    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setCode("WH001");
        testWarehouse.setName("测试仓库");
        testWarehouse.setType("成品仓");
        testWarehouse.setManager("张三");
        testWarehouse.setPhone("13800138000");
        testWarehouse.setStatus("ACTIVE");
    }

    @Test
    void testCreateWarehouse_Success() throws Exception {
        // Given
        when(warehouseService.createWarehouse(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse w = invocation.getArgument(0);
            w.setId(2L);
            w.setCode("WH002");
            return w;
        });

        // When
        String result = warehouseTools.createWarehouse(
                "新仓库",
                "原料仓",
                "北京市朝阳区",
                "李四",
                "13900139000",
                "备注信息"
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"id\":2"));
        assertTrue(result.contains("\"name\":\"新仓库\""));
        assertTrue(result.contains("\"type\":\"原料仓\""));

        verify(warehouseService).createWarehouse(any(Warehouse.class));
    }

    @Test
    void testCreateWarehouse_EmptyName() throws Exception {
        // When
        String result = warehouseTools.createWarehouse(
                "",
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("仓库名称不能为空"));

        verify(warehouseService, never()).createWarehouse(any());
    }

    @Test
    void testCreateWarehouse_NullName() throws Exception {
        // When
        String result = warehouseTools.createWarehouse(
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("仓库名称不能为空"));
    }

    @Test
    void testCreateWarehouse_DefaultType() throws Exception {
        // Given
        when(warehouseService.createWarehouse(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse w = invocation.getArgument(0);
            w.setId(3L);
            return w;
        });

        // When
        String result = warehouseTools.createWarehouse(
                "新仓库2",
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"type\":\"普通仓库\""));
    }

    @Test
    void testSearchWarehouses_Success() throws Exception {
        // Given
        Page<Warehouse> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testWarehouse));
        mockPage.setTotal(1);

        when(warehouseService.pageWarehouses(1, 10, "测试", "ACTIVE")).thenReturn(mockPage);

        // When
        String result = warehouseTools.searchWarehouses("测试", 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"name\":\"测试仓库\""));
    }

    @Test
    void testSearchWarehouses_DefaultPaging() throws Exception {
        // Given
        Page<Warehouse> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(warehouseService.pageWarehouses(1, 10, null, "ACTIVE")).thenReturn(mockPage);

        // When
        String result = warehouseTools.searchWarehouses(null, null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
    }

    @Test
    void testGetWarehouseDetail_Success() throws Exception {
        // Given
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);

        // When
        String result = warehouseTools.getWarehouseDetail(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"name\":\"测试仓库\""));
        assertTrue(result.contains("\"manager\":\"张三\""));
    }

    @Test
    void testGetWarehouseDetail_NotFound() throws Exception {
        // Given
        when(warehouseService.getById(999L)).thenReturn(null);

        // When
        String result = warehouseTools.getWarehouseDetail(999L);

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("仓库不存在"));
    }

    @Test
    void testListAllWarehouses_Success() throws Exception {
        // Given
        Page<Warehouse> mockPage = new Page<>(1, 100);
        mockPage.setRecords(List.of(testWarehouse));
        mockPage.setTotal(1);

        when(warehouseService.pageWarehouses(1, 100, null, "ACTIVE")).thenReturn(mockPage);

        // When
        String result = warehouseTools.listAllWarehouses();

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("共找到1个仓库"));
    }

    @Test
    void testListAllWarehouses_Empty() throws Exception {
        // Given
        Page<Warehouse> mockPage = new Page<>(1, 100);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(warehouseService.pageWarehouses(1, 100, null, "ACTIVE")).thenReturn(mockPage);

        // When
        String result = warehouseTools.listAllWarehouses();

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
        assertTrue(result.contains("暂无可用仓库"));
    }
}
