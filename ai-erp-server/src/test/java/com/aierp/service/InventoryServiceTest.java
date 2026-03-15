package com.aierp.service;

import com.aierp.entity.Inventory;
import com.aierp.entity.InventoryTransaction;
import com.aierp.mapper.InventoryMapper;
import com.aierp.mapper.InventoryTransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 库存服务测试
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryTransactionMapper transactionMapper;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setWarehouseId(1L);
        testInventory.setProductId(1L);
        testInventory.setProductSku("SKU001");
        testInventory.setProductName("测试商品");
        testInventory.setQuantity(new BigDecimal("100"));
        testInventory.setAvailableQty(new BigDecimal("100"));
        testInventory.setLockedQty(BigDecimal.ZERO);
        testInventory.setCostPrice(new BigDecimal("10.00"));
    }

    @Test
    void testStockIn_NewInventory() {
        // Given
        when(inventoryMapper.findByWarehouseAndProduct(1L, 1L)).thenReturn(null);
        when(inventoryMapper.insert(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory i = invocation.getArgument(0);
            i.setId(1L);
            return 1;
        });
        when(transactionMapper.insert(any(InventoryTransaction.class))).thenReturn(1);

        // When
        Inventory result = inventoryService.stockIn(
                1L, 1L, "SKU001", "测试商品",
                new BigDecimal("50"), new BigDecimal("10.00"),
                "PURCHASE", 1L, "PO001", "admin"
        );

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("50"), result.getQuantity());
        verify(inventoryMapper).insert(any(Inventory.class));
        verify(transactionMapper).insert(any(InventoryTransaction.class));
    }

    @Test
    void testStockIn_ExistingInventory() {
        // Given
        when(inventoryMapper.findByWarehouseAndProduct(1L, 1L)).thenReturn(testInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);
        when(transactionMapper.insert(any(InventoryTransaction.class))).thenReturn(1);

        // When
        Inventory result = inventoryService.stockIn(
                1L, 1L, "SKU001", "测试商品",
                new BigDecimal("50"), new BigDecimal("12.00"),
                "PURCHASE", 1L, "PO001", "admin"
        );

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150"), result.getQuantity());
        assertEquals(new BigDecimal("12.00"), result.getCostPrice());
        verify(inventoryMapper).updateById(any(Inventory.class));
    }

    @Test
    void testStockOut_Success() {
        // Given
        when(inventoryMapper.findByWarehouseAndProduct(1L, 1L)).thenReturn(testInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);
        when(transactionMapper.insert(any(InventoryTransaction.class))).thenReturn(1);

        // When
        Inventory result = inventoryService.stockOut(
                1L, 1L, new BigDecimal("30"),
                "SALES", 1L, "SO001", "admin"
        );

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("70"), result.getQuantity());
        assertEquals(new BigDecimal("70"), result.getAvailableQty());
        verify(inventoryMapper).updateById(any(Inventory.class));
    }

    @Test
    void testStockOut_InsufficientStock() {
        // Given
        when(inventoryMapper.findByWarehouseAndProduct(1L, 1L)).thenReturn(testInventory);

        // When & Then
        assertThrows(RuntimeException.class, () -> inventoryService.stockOut(
                1L, 1L, new BigDecimal("200"),
                "SALES", 1L, "SO001", "admin"
        ));
    }

    @Test
    void testStockOut_InventoryNotFound() {
        // Given
        when(inventoryMapper.findByWarehouseAndProduct(1L, 1L)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> inventoryService.stockOut(
                1L, 1L, new BigDecimal("10"),
                "SALES", 1L, "SO001", "admin"
        ));
    }

    @Test
    void testGetByWarehouseAndProduct() {
        // Given
        when(inventoryMapper.findByWarehouseAndProduct(1L, 1L)).thenReturn(testInventory);

        // When
        Inventory found = inventoryService.getByWarehouseAndProduct(1L, 1L);

        // Then
        assertNotNull(found);
        assertEquals("SKU001", found.getProductSku());
    }
}
