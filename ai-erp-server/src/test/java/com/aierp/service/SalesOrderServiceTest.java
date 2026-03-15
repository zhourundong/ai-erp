package com.aierp.service;

import com.aierp.entity.SalesOrder;
import com.aierp.entity.SalesOrderItem;
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
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private SalesOrderService salesOrderService;

    private SalesOrder testOrder;

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
}
