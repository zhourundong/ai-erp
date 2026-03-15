package com.aierp.service;

import com.aierp.dto.ShipRequest;
import com.aierp.entity.Inventory;
import com.aierp.entity.SalesOrder;
import com.aierp.entity.SalesOrderItem;
import com.aierp.entity.Warehouse;
import com.aierp.mapper.InventoryMapper;
import com.aierp.mapper.SalesOrderItemMapper;
import com.aierp.mapper.SalesOrderMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import com.aierp.common.BusinessException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 销售订单服务测试
 */
@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private SalesOrderItemMapper salesOrderItemMapper;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private SalesOrder testOrder;
    private SalesOrderItem testItem;
    private Warehouse testWarehouse;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testOrder = new SalesOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("SO20260314001");
        testOrder.setOrderDate(LocalDate.now());
        testOrder.setCustomerId(1L);
        testOrder.setCustomerName("测试客户");
        testOrder.setTotalAmount(new BigDecimal("2000.00"));
        testOrder.setStatus("DRAFT");

        testItem = new SalesOrderItem();
        testItem.setId(1L);
        testItem.setOrderId(1L);
        testItem.setProductId(100L);
        testItem.setProductSku("SKU001");
        testItem.setProductName("测试商品");
        testItem.setQuantity(new BigDecimal("10"));
        testItem.setUnitPrice(new BigDecimal("200.00"));
        testItem.setStatus("PENDING");

        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("主仓库");
        testWarehouse.setStatus("ACTIVE");

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(100L);
        testInventory.setQuantity(new BigDecimal("100"));
        testInventory.setAvailableQty(new BigDecimal("100"));
    }

    @Test
    void testCreateOrder_Success() {
        // Given
        SalesOrder newOrder = new SalesOrder();
        newOrder.setCustomerId(1L);
        newOrder.setCustomerName("新客户");

        SalesOrderItem item = new SalesOrderItem();
        item.setProductName("测试商品");
        item.setQuantity(new BigDecimal("10"));
        item.setUnitPrice(new BigDecimal("200.00"));

        when(salesOrderMapper.insert(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder o = invocation.getArgument(0);
            o.setId(2L);
            return 1;
        });
        when(salesOrderItemMapper.insert(any(SalesOrderItem.class))).thenReturn(1);

        // When
        SalesOrder created = salesOrderService.createOrder(newOrder, List.of(item));

        // Then
        assertNotNull(created);
        assertNotNull(created.getOrderNo());
        assertEquals("DRAFT", created.getStatus());
        verify(salesOrderMapper).insert(any(SalesOrder.class));
    }

    @Test
    void testPageOrders() {
        // Given
        Page<SalesOrder> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testOrder));
        mockPage.setTotal(1);

        when(salesOrderMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<SalesOrder> result = salesOrderService.pageOrders(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testGetById() {
        // Given
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When
        SalesOrder found = salesOrderService.getById(1L);

        // Then
        assertNotNull(found);
        assertEquals("SO20260314001", found.getOrderNo());
    }

    @Test
    void testUpdateOrder() {
        // Given
        testOrder.setTotalAmount(new BigDecimal("3000.00"));
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // When
        SalesOrder updated = salesOrderService.updateOrder(testOrder);

        // Then
        assertEquals(new BigDecimal("3000.00"), updated.getTotalAmount());
        verify(salesOrderMapper).updateById(any(SalesOrder.class));
    }

    @Test
    void testDeleteOrder() {
        // Given
        when(salesOrderItemMapper.delete(any())).thenReturn(0);
        when(salesOrderMapper.deleteById(1L)).thenReturn(1);

        // When
        salesOrderService.deleteOrder(1L);

        // Then
        verify(salesOrderMapper).deleteById(1L);
    }

    // ========== 新增功能测试 ==========

    @Test
    void testConfirm_Success() {
        // Given
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(inventoryService.getByWarehouseAndProduct(1L, 100L)).thenReturn(testInventory);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // When
        SalesOrder result = salesOrderService.confirm(1L, 1L);

        // Then
        assertEquals("CONFIRMED", result.getStatus());
        verify(salesOrderMapper).updateById(any(SalesOrder.class));
    }

    @Test
    void testConfirm_InsufficientStock() {
        // Given
        testInventory.setAvailableQty(new BigDecimal("5")); // 库存不足
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(inventoryService.getByWarehouseAndProduct(1L, 100L)).thenReturn(testInventory);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.confirm(1L, 1L);
        });
    }

    @Test
    void testConfirm_NoInventory() {
        // Given
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(inventoryService.getByWarehouseAndProduct(1L, 100L)).thenReturn(null); // 无库存

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.confirm(1L, 1L);
        });
    }

    @Test
    void testConfirm_NotDraft() {
        // Given
        testOrder.setStatus("CONFIRMED");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.confirm(1L, 1L);
        });
    }

    @Test
    void testCancel_Success() {
        // Given
        testOrder.setStatus("DRAFT");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // When
        SalesOrder result = salesOrderService.cancel(1L);

        // Then
        assertEquals("CANCELLED", result.getStatus());
        verify(salesOrderMapper).updateById(any(SalesOrder.class));
    }

    @Test
    void testCancel_CompletedOrder() {
        // Given
        testOrder.setStatus("COMPLETED");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.cancel(1L);
        });
    }

    @Test
    void testCancel_ShippingOrder() {
        // Given
        testOrder.setStatus("SHIPPING");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.cancel(1L);
        });
    }

    @Test
    void testShipGoods_Success() {
        // Given
        testOrder.setStatus("CONFIRMED");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);
        when(salesOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(salesOrderItemMapper.updateById(any(SalesOrderItem.class))).thenReturn(1);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // 模拟库存出库返回
        Inventory mockInventory = new Inventory();
        mockInventory.setQuantity(new BigDecimal("90"));
        when(inventoryService.stockOut(anyLong(), anyLong(), any(), any(), anyLong(), any(), any()))
                .thenReturn(mockInventory);

        ShipRequest.ShipItem shipItem = new ShipRequest.ShipItem();
        shipItem.setItemId(1L);
        shipItem.setQuantity(new BigDecimal("10"));

        // When
        SalesOrder result = salesOrderService.shipGoods(1L, 1L, List.of(shipItem), "李四");

        // Then
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getActualDeliveryDate());
        verify(inventoryService).stockOut(eq(1L), eq(100L), eq(new BigDecimal("10")), any(), eq(1L), any(), eq("李四"));
    }

    @Test
    void testShipGoods_PartialShip() {
        // Given
        testOrder.setStatus("CONFIRMED");
        testItem.setQuantity(new BigDecimal("20"));
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);
        when(salesOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(salesOrderItemMapper.updateById(any(SalesOrderItem.class))).thenReturn(1);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        ShipRequest.ShipItem shipItem = new ShipRequest.ShipItem();
        shipItem.setItemId(1L);
        shipItem.setQuantity(new BigDecimal("10"));

        // When
        SalesOrder result = salesOrderService.shipGoods(1L, 1L, List.of(shipItem), "李四");

        // Then
        assertEquals("SHIPPING", result.getStatus());
    }

    @Test
    void testShipGoods_OverQuantity() {
        // Given
        testOrder.setStatus("CONFIRMED");
        testItem.setQuantity(new BigDecimal("5"));
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);
        when(salesOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));

        ShipRequest.ShipItem shipItem = new ShipRequest.ShipItem();
        shipItem.setItemId(1L);
        shipItem.setQuantity(new BigDecimal("10")); // 超过订单数量

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.shipGoods(1L, 1L, List.of(shipItem), "李四");
        });
    }

    @Test
    void testShipGoods_WrongStatus() {
        // Given
        testOrder.setStatus("DRAFT");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        ShipRequest.ShipItem shipItem = new ShipRequest.ShipItem();
        shipItem.setItemId(1L);
        shipItem.setQuantity(new BigDecimal("10"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.shipGoods(1L, 1L, List.of(shipItem), "李四");
        });
    }

    // ========== 反确认测试 ==========

    @Test
    void testUnconfirm_Success() {
        // Given
        testOrder.setStatus("CONFIRMED");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // When
        SalesOrder result = salesOrderService.unconfirm(1L);

        // Then
        assertEquals("DRAFT", result.getStatus());
        verify(salesOrderMapper).updateById(any(SalesOrder.class));
    }

    @Test
    void testUnconfirm_NotConfirmed() {
        // Given
        testOrder.setStatus("DRAFT");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.unconfirm(1L);
        });
    }

    @Test
    void testUnconfirm_OrderNotFound() {
        // Given
        when(salesOrderMapper.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.unconfirm(1L);
        });
    }

    @Test
    void testUnconfirm_AlreadyShipped() {
        // Given
        testOrder.setStatus("SHIPPING");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.unconfirm(1L);
        });
    }

    @Test
    void testUnconfirm_CompletedOrder() {
        // Given
        testOrder.setStatus("COMPLETED");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.unconfirm(1L);
        });
    }

    // ========== 更新订单测试 ==========

    @Test
    void testUpdateOrderWithItems_Success() {
        // Given
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderItemMapper.delete(any())).thenReturn(1);
        when(salesOrderItemMapper.insert(any(SalesOrderItem.class))).thenReturn(1);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        SalesOrderItem newItem = new SalesOrderItem();
        newItem.setProductId(101L);
        newItem.setProductName("新商品");
        newItem.setQuantity(new BigDecimal("5"));
        newItem.setUnitPrice(new BigDecimal("200.00"));

        // When
        SalesOrder result = salesOrderService.updateOrderWithItems(testOrder, List.of(newItem));

        // Then
        assertNotNull(result);
        verify(salesOrderItemMapper).delete(any());
        verify(salesOrderItemMapper).insert(any(SalesOrderItem.class));
        verify(salesOrderMapper).updateById(any(SalesOrder.class));
    }

    @Test
    void testUpdateOrderWithItems_NotDraft() {
        // Given
        testOrder.setStatus("CONFIRMED");
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);

        SalesOrderItem newItem = new SalesOrderItem();
        newItem.setProductName("新商品");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.updateOrderWithItems(testOrder, List.of(newItem));
        });
    }

    @Test
    void testUpdateOrderWithItems_OrderNotFound() {
        // Given
        when(salesOrderMapper.selectById(1L)).thenReturn(null);

        SalesOrderItem newItem = new SalesOrderItem();
        newItem.setProductName("新商品");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            salesOrderService.updateOrderWithItems(testOrder, List.of(newItem));
        });
    }
}
