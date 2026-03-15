package com.aierp.ai.tools;

import com.aierp.entity.Inventory;
import com.aierp.entity.InventoryTransaction;
import com.aierp.service.InventoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 库存工具测试
 */
@ExtendWith(MockitoExtension.class)
class InventoryToolsTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryTools inventoryTools;

    private Inventory testInventory;
    private InventoryTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setWarehouseId(1L);
        testInventory.setProductId(1L);
        testInventory.setProductSku("SKU001");
        testInventory.setProductName("测试商品");
        testInventory.setQuantity(BigDecimal.valueOf(100));
        testInventory.setAvailableQty(BigDecimal.valueOf(80));
        testInventory.setLockedQty(BigDecimal.valueOf(20));
        testInventory.setCostPrice(BigDecimal.valueOf(50));

        testTransaction = new InventoryTransaction();
        testTransaction.setId(1L);
        testTransaction.setTransactionNo("TXN001");
        testTransaction.setTransactionType("PURCHASE_IN");
        testTransaction.setWarehouseId(1L);
        testTransaction.setWarehouseName("测试仓库");
        testTransaction.setProductId(1L);
        testTransaction.setProductSku("SKU001");
        testTransaction.setProductName("测试商品");
        testTransaction.setQuantity(BigDecimal.valueOf(50));
        testTransaction.setBeforeQty(BigDecimal.valueOf(50));
        testTransaction.setAfterQty(BigDecimal.valueOf(100));
        testTransaction.setUnitCost(BigDecimal.valueOf(50));
        testTransaction.setTotalCost(BigDecimal.valueOf(2500));
        testTransaction.setOperator("张三");
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCheckInventory_Success() throws Exception {
        // Given
        Page<Inventory> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testInventory));
        mockPage.setTotal(1);

        when(inventoryService.pageInventory(1, 10, 1L, 1L)).thenReturn(mockPage);

        // When
        String result = inventoryTools.checkInventory(1L, 1L, 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"productSku\":\"SKU001\""));
        assertTrue(result.contains("\"quantity\":100"));
    }

    @Test
    void testCheckInventory_AllWarehouses() throws Exception {
        // Given
        Page<Inventory> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testInventory));
        mockPage.setTotal(1);

        when(inventoryService.pageInventory(1, 10, null, null)).thenReturn(mockPage);

        // When
        String result = inventoryTools.checkInventory(null, null, null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
    }

    @Test
    void testCheckInventory_EmptyResult() throws Exception {
        // Given
        Page<Inventory> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(inventoryService.pageInventory(1, 10, 999L, 999L)).thenReturn(mockPage);

        // When
        String result = inventoryTools.checkInventory(999L, 999L, 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
    }

    @Test
    void testQueryTransactions_Success() throws Exception {
        // Given
        Page<InventoryTransaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testTransaction));
        mockPage.setTotal(1);

        when(inventoryService.pageTransactions(1, 10, 1L, 1L, "PURCHASE_IN")).thenReturn(mockPage);

        // When
        String result = inventoryTools.queryTransactions(1L, 1L, "PURCHASE_IN", 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"transactionType\":\"PURCHASE_IN\""));
        assertTrue(result.contains("\"transactionNo\":\"TXN001\""));
    }

    @Test
    void testQueryTransactions_AllTypes() throws Exception {
        // Given
        Page<InventoryTransaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testTransaction));
        mockPage.setTotal(1);

        when(inventoryService.pageTransactions(1, 10, null, null, null)).thenReturn(mockPage);

        // When
        String result = inventoryTools.queryTransactions(null, null, null, null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
    }

    @Test
    void testQueryTransactions_SalesOut() throws Exception {
        // Given
        testTransaction.setTransactionType("SALES_OUT");
        Page<InventoryTransaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testTransaction));
        mockPage.setTotal(1);

        when(inventoryService.pageTransactions(1, 10, 1L, null, "SALES_OUT")).thenReturn(mockPage);

        // When
        String result = inventoryTools.queryTransactions(1L, null, "SALES_OUT", 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"transactionType\":\"SALES_OUT\""));
    }

    @Test
    void testQueryTransactions_EmptyResult() throws Exception {
        // Given
        Page<InventoryTransaction> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(inventoryService.pageTransactions(1, 10, null, null, null)).thenReturn(mockPage);

        // When
        String result = inventoryTools.queryTransactions(null, null, null, 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
    }
}
