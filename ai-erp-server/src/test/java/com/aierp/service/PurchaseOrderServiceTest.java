package com.aierp.service;

import com.aierp.dto.ReceiveRequest;
import com.aierp.entity.Inventory;
import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.entity.Warehouse;
import com.aierp.mapper.InventoryMapper;
import com.aierp.mapper.PurchaseOrderItemMapper;
import com.aierp.mapper.PurchaseOrderMapper;
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
import static org.mockito.Mockito.*;

/**
 * 采购订单服务测试
 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @Mock
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrder testOrder;
    private PurchaseOrderItem testItem;
    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        testOrder = new PurchaseOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("PO20260314001");
        testOrder.setOrderDate(LocalDate.now());
        testOrder.setSupplierId(1L);
        testOrder.setSupplierName("测试供应商");
        testOrder.setTotalAmount(new BigDecimal("1000.00"));
        testOrder.setStatus("DRAFT");

        testItem = new PurchaseOrderItem();
        testItem.setId(1L);
        testItem.setOrderId(1L);
        testItem.setProductId(100L);
        testItem.setProductSku("SKU001");
        testItem.setProductName("测试商品");
        testItem.setQuantity(new BigDecimal("10"));
        testItem.setUnitPrice(new BigDecimal("100.00"));
        testItem.setStatus("PENDING");

        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("主仓库");
        testWarehouse.setStatus("ACTIVE");
    }

    @Test
    void testCreateOrder_Success() {
        // Given
        PurchaseOrder newOrder = new PurchaseOrder();
        newOrder.setSupplierId(1L);
        newOrder.setSupplierName("新供应商");

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductName("测试商品");
        item.setQuantity(new BigDecimal("10"));
        item.setUnitPrice(new BigDecimal("100.00"));

        when(purchaseOrderMapper.insert(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder o = invocation.getArgument(0);
            o.setId(2L);
            return 1;
        });
        when(purchaseOrderItemMapper.insert(any(PurchaseOrderItem.class))).thenReturn(1);

        // When
        PurchaseOrder created = purchaseOrderService.createOrder(newOrder, List.of(item));

        // Then
        assertNotNull(created);
        assertNotNull(created.getOrderNo());
        assertEquals("DRAFT", created.getStatus());
        verify(purchaseOrderMapper).insert(any(PurchaseOrder.class));
    }

    @Test
    void testPageOrders() {
        // Given
        Page<PurchaseOrder> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testOrder));
        mockPage.setTotal(1);

        when(purchaseOrderMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<PurchaseOrder> result = purchaseOrderService.pageOrders(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testGetById() {
        // Given
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When
        PurchaseOrder found = purchaseOrderService.getById(1L);

        // Then
        assertNotNull(found);
        assertEquals("PO20260314001", found.getOrderNo());
    }

    @Test
    void testUpdateOrder() {
        // Given
        testOrder.setTotalAmount(new BigDecimal("2000.00"));
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        // When
        PurchaseOrder updated = purchaseOrderService.updateOrder(testOrder);

        // Then
        assertEquals(new BigDecimal("2000.00"), updated.getTotalAmount());
        verify(purchaseOrderMapper).updateById(any(PurchaseOrder.class));
    }

    @Test
    void testDeleteOrder() {
        // Given
        when(purchaseOrderItemMapper.delete(any())).thenReturn(0);
        when(purchaseOrderMapper.deleteById(1L)).thenReturn(1);

        // When
        purchaseOrderService.deleteOrder(1L);

        // Then
        verify(purchaseOrderMapper).deleteById(1L);
    }

    // ========== 新增功能测试 ==========

    @Test
    void testSubmitForApproval_Success() {
        // Given
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        // When
        PurchaseOrder result = purchaseOrderService.submitForApproval(1L);

        // Then
        assertEquals("PENDING", result.getStatus());
        verify(purchaseOrderMapper).updateById(any(PurchaseOrder.class));
    }

    @Test
    void testSubmitForApproval_NotDraft() {
        // Given
        testOrder.setStatus("PENDING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.submitForApproval(1L);
        });
    }

    @Test
    void testApprove_Success() {
        // Given
        testOrder.setStatus("PENDING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        // When
        PurchaseOrder result = purchaseOrderService.approve(1L);

        // Then
        assertEquals("APPROVED", result.getStatus());
        verify(purchaseOrderMapper).updateById(any(PurchaseOrder.class));
    }

    @Test
    void testApprove_NotPending() {
        // Given
        testOrder.setStatus("DRAFT");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.approve(1L);
        });
    }

    @Test
    void testReject_Success() {
        // Given
        testOrder.setStatus("PENDING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        // When
        PurchaseOrder result = purchaseOrderService.reject(1L);

        // Then
        assertEquals("REJECTED", result.getStatus());
        verify(purchaseOrderMapper).updateById(any(PurchaseOrder.class));
    }

    @Test
    void testReceiveGoods_Success() {
        // Given
        testOrder.setStatus("APPROVED");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);
        when(purchaseOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(purchaseOrderItemMapper.updateById(any(PurchaseOrderItem.class))).thenReturn(1);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        // 模拟库存入库返回
        Inventory mockInventory = new Inventory();
        mockInventory.setQuantity(new BigDecimal("10"));
        when(inventoryService.stockIn(anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), anyLong(), any(), any()))
                .thenReturn(mockInventory);

        ReceiveRequest.ReceiveItem receiveItem = new ReceiveRequest.ReceiveItem();
        receiveItem.setItemId(1L);
        receiveItem.setQuantity(new BigDecimal("10"));

        // When
        PurchaseOrder result = purchaseOrderService.receiveGoods(1L, 1L, List.of(receiveItem), "张三");

        // Then
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getActualDeliveryDate());
        verify(inventoryService).stockIn(eq(1L), eq(100L), any(), any(), eq(new BigDecimal("10")), any(), any(), any(), eq(1L), any(), eq("张三"));
    }

    @Test
    void testReceiveGoods_PartialReceive() {
        // Given
        testOrder.setStatus("APPROVED");
        testItem.setQuantity(new BigDecimal("20"));
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);
        when(purchaseOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));
        when(purchaseOrderItemMapper.updateById(any(PurchaseOrderItem.class))).thenReturn(1);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        ReceiveRequest.ReceiveItem receiveItem = new ReceiveRequest.ReceiveItem();
        receiveItem.setItemId(1L);
        receiveItem.setQuantity(new BigDecimal("10"));

        // When
        PurchaseOrder result = purchaseOrderService.receiveGoods(1L, 1L, List.of(receiveItem), "张三");

        // Then
        assertEquals("RECEIVING", result.getStatus());
    }

    @Test
    void testReceiveGoods_OverQuantity() {
        // Given
        testOrder.setStatus("APPROVED");
        testItem.setQuantity(new BigDecimal("5"));
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(testWarehouse);
        when(purchaseOrderItemMapper.findByOrderId(1L)).thenReturn(List.of(testItem));

        ReceiveRequest.ReceiveItem receiveItem = new ReceiveRequest.ReceiveItem();
        receiveItem.setItemId(1L);
        receiveItem.setQuantity(new BigDecimal("10")); // 超过订单数量

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.receiveGoods(1L, 1L, List.of(receiveItem), "张三");
        });
    }

    @Test
    void testReceiveGoods_WrongStatus() {
        // Given
        testOrder.setStatus("DRAFT");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        ReceiveRequest.ReceiveItem receiveItem = new ReceiveRequest.ReceiveItem();
        receiveItem.setItemId(1L);
        receiveItem.setQuantity(new BigDecimal("10"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.receiveGoods(1L, 1L, List.of(receiveItem), "张三");
        });
    }

    @Test
    void testReceiveGoods_InvalidWarehouse() {
        // Given
        testOrder.setStatus("APPROVED");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(warehouseService.getById(1L)).thenReturn(null); // 仓库不存在

        ReceiveRequest.ReceiveItem receiveItem = new ReceiveRequest.ReceiveItem();
        receiveItem.setItemId(1L);
        receiveItem.setQuantity(new BigDecimal("10"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.receiveGoods(1L, 1L, List.of(receiveItem), "张三");
        });
    }

    // ========== 反审核测试 ==========

    @Test
    void testUnapprove_Success() {
        // Given
        testOrder.setStatus("APPROVED");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        // When
        PurchaseOrder result = purchaseOrderService.unapprove(1L);

        // Then
        assertEquals("PENDING", result.getStatus());
        verify(purchaseOrderMapper).updateById(any(PurchaseOrder.class));
    }

    @Test
    void testUnapprove_NotApproved() {
        // Given
        testOrder.setStatus("PENDING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.unapprove(1L);
        });
    }

    @Test
    void testUnapprove_OrderNotFound() {
        // Given
        when(purchaseOrderMapper.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.unapprove(1L);
        });
    }

    @Test
    void testUnapprove_AlreadyReceived() {
        // Given
        testOrder.setStatus("RECEIVING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.unapprove(1L);
        });
    }

    // ========== 更新订单测试 ==========

    @Test
    void testUpdateOrderWithItems_Success() {
        // Given
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(purchaseOrderItemMapper.delete(any())).thenReturn(1);
        when(purchaseOrderItemMapper.insert(any(PurchaseOrderItem.class))).thenReturn(1);
        when(purchaseOrderMapper.updateById(any(PurchaseOrder.class))).thenReturn(1);

        PurchaseOrderItem newItem = new PurchaseOrderItem();
        newItem.setProductId(101L);
        newItem.setProductName("新商品");
        newItem.setQuantity(new BigDecimal("5"));
        newItem.setUnitPrice(new BigDecimal("200.00"));

        // When
        PurchaseOrder result = purchaseOrderService.updateOrderWithItems(testOrder, List.of(newItem));

        // Then
        assertNotNull(result);
        verify(purchaseOrderItemMapper).delete(any());
        verify(purchaseOrderItemMapper).insert(any(PurchaseOrderItem.class));
        verify(purchaseOrderMapper).updateById(any(PurchaseOrder.class));
    }

    @Test
    void testUpdateOrderWithItems_NotDraft() {
        // Given
        testOrder.setStatus("PENDING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(testOrder);

        PurchaseOrderItem newItem = new PurchaseOrderItem();
        newItem.setProductName("新商品");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.updateOrderWithItems(testOrder, List.of(newItem));
        });
    }

    @Test
    void testUpdateOrderWithItems_OrderNotFound() {
        // Given
        when(purchaseOrderMapper.selectById(1L)).thenReturn(null);

        PurchaseOrderItem newItem = new PurchaseOrderItem();
        newItem.setProductName("新商品");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseOrderService.updateOrderWithItems(testOrder, List.of(newItem));
        });
    }
}
