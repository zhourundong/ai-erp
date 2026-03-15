package com.aierp.service;

import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
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

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrder testOrder;

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
}
