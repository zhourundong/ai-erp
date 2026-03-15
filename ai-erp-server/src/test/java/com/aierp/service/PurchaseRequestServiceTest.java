package com.aierp.service;

import com.aierp.dto.CreateOrderFromRequest;
import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.entity.PurchaseRequest;
import com.aierp.entity.PurchaseRequestItem;
import com.aierp.mapper.PurchaseOrderItemMapper;
import com.aierp.mapper.PurchaseOrderMapper;
import com.aierp.mapper.PurchaseRequestItemMapper;
import com.aierp.mapper.PurchaseRequestMapper;
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
 * 采购申请服务测试
 */
@ExtendWith(MockitoExtension.class)
class PurchaseRequestServiceTest {

    @Mock
    private PurchaseRequestMapper purchaseRequestMapper;

    @Mock
    private PurchaseRequestItemMapper purchaseRequestItemMapper;

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @Mock
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @InjectMocks
    private PurchaseRequestService purchaseRequestService;

    private PurchaseRequest testRequest;
    private PurchaseRequestItem testItem;

    @BeforeEach
    void setUp() {
        testRequest = new PurchaseRequest();
        testRequest.setId(1L);
        testRequest.setRequestNo("PR20260314001");
        testRequest.setRequestDate(LocalDate.now());
        testRequest.setApplicantId(1L);
        testRequest.setApplicantName("张三");
        testRequest.setStatus("DRAFT");
        testRequest.setRequirementDescription("需要100个M8螺丝");

        testItem = new PurchaseRequestItem();
        testItem.setId(1L);
        testItem.setRequestId(1L);
        testItem.setProductId(100L);
        testItem.setProductSku("SKU001");
        testItem.setProductName("M8螺丝");
        testItem.setQuantity(new BigDecimal("100"));
        testItem.setUnit("个");
        testItem.setEstimatedPrice(new BigDecimal("0.50"));
    }

    @Test
    void testCreateRequest_Success() {
        // Given
        PurchaseRequest newRequest = new PurchaseRequest();
        newRequest.setApplicantId(1L);
        newRequest.setApplicantName("张三");
        newRequest.setRequirementDescription("需要50个螺母");

        PurchaseRequestItem item = new PurchaseRequestItem();
        item.setProductName("螺母");
        item.setQuantity(new BigDecimal("50"));
        item.setEstimatedPrice(new BigDecimal("2.00"));

        when(purchaseRequestMapper.insert(any(PurchaseRequest.class))).thenAnswer(invocation -> {
            PurchaseRequest r = invocation.getArgument(0);
            r.setId(2L);
            return 1;
        });
        when(purchaseRequestItemMapper.insert(any(PurchaseRequestItem.class))).thenReturn(1);

        // When
        PurchaseRequest created = purchaseRequestService.createRequest(newRequest, List.of(item));

        // Then
        assertNotNull(created);
        assertNotNull(created.getRequestNo());
        assertEquals("DRAFT", created.getStatus());
        assertNotNull(created.getRequestDate());

        verify(purchaseRequestMapper).insert(any(PurchaseRequest.class));
        verify(purchaseRequestItemMapper).insert(any(PurchaseRequestItem.class));
    }

    @Test
    void testCreateRequest_WithoutItems() {
        // Given
        PurchaseRequest newRequest = new PurchaseRequest();
        newRequest.setApplicantId(1L);
        newRequest.setApplicantName("张三");

        when(purchaseRequestMapper.insert(any(PurchaseRequest.class))).thenAnswer(invocation -> {
            PurchaseRequest r = invocation.getArgument(0);
            r.setId(2L);
            return 1;
        });

        // When
        PurchaseRequest created = purchaseRequestService.createRequest(newRequest, null);

        // Then
        assertNotNull(created);
        assertNotNull(created.getRequestNo());
        assertEquals("DRAFT", created.getStatus());

        verify(purchaseRequestMapper).insert(any(PurchaseRequest.class));
        verify(purchaseRequestItemMapper, never()).insert(any());
    }

    @Test
    void testPageRequests() {
        // Given
        Page<PurchaseRequest> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testRequest));
        mockPage.setTotal(1);

        when(purchaseRequestMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<PurchaseRequest> result = purchaseRequestService.pageRequests(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testSubmitForApproval_Success() {
        // Given
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestMapper.updateById(any(PurchaseRequest.class))).thenReturn(1);

        // When
        PurchaseRequest result = purchaseRequestService.submitForApproval(1L);

        // Then
        assertEquals("PENDING", result.getStatus());
        verify(purchaseRequestMapper).updateById(any(PurchaseRequest.class));
    }

    @Test
    void testSubmitForApproval_NotDraft() {
        // Given
        testRequest.setStatus("PENDING");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseRequestService.submitForApproval(1L);
        });
    }

    @Test
    void testApprove_Success() {
        // Given
        testRequest.setStatus("PENDING");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestMapper.updateById(any(PurchaseRequest.class))).thenReturn(1);

        // When
        PurchaseRequest result = purchaseRequestService.approve(1L, 2L, "李四", "同意");

        // Then
        assertEquals("APPROVED", result.getStatus());
        assertEquals(2L, result.getApproverId());
        assertEquals("李四", result.getApproverName());
        assertNotNull(result.getApprovalTime());

        verify(purchaseRequestMapper).updateById(any(PurchaseRequest.class));
    }

    @Test
    void testReject_Success() {
        // Given
        testRequest.setStatus("PENDING");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestMapper.updateById(any(PurchaseRequest.class))).thenReturn(1);

        // When
        PurchaseRequest result = purchaseRequestService.reject(1L, 2L, "李四", "预算不足");

        // Then
        assertEquals("REJECTED", result.getStatus());
        assertEquals(2L, result.getApproverId());
        assertEquals("李四", result.getApproverName());
        assertEquals("预算不足", result.getApprovalComment());

        verify(purchaseRequestMapper).updateById(any(PurchaseRequest.class));
    }

    @Test
    void testDeleteRequest() {
        // Given
        when(purchaseRequestItemMapper.delete(any())).thenReturn(0);
        when(purchaseRequestMapper.deleteById(1L)).thenReturn(1);

        // When
        purchaseRequestService.deleteRequest(1L);

        // Then
        verify(purchaseRequestItemMapper).delete(any());
        verify(purchaseRequestMapper).deleteById(1L);
    }

    // ========== 新增功能测试：从申请生成订单 ==========

    @Test
    void testCreateOrderFromRequest_Success() {
        // Given
        testRequest.setStatus("APPROVED");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestItemMapper.findByRequestId(1L)).thenReturn(List.of(testItem));
        when(purchaseOrderMapper.insert(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder o = invocation.getArgument(0);
            o.setId(10L);
            return 1;
        });
        when(purchaseOrderItemMapper.insert(any(PurchaseOrderItem.class))).thenReturn(1);
        when(purchaseRequestMapper.updateById(any(PurchaseRequest.class))).thenReturn(1);
        when(purchaseOrderMapper.selectCount(any())).thenReturn(0L);

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");
        params.setBuyerId(1L);
        params.setBuyerName("张三");

        // When
        PurchaseOrder result = purchaseRequestService.createOrderFromRequest(1L, params);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getSupplierId());
        assertEquals("测试供应商", result.getSupplierName());
        assertEquals(1L, result.getRequestId());
        assertEquals("DRAFT", result.getStatus());
        verify(purchaseOrderMapper).insert(any(PurchaseOrder.class));
        verify(purchaseOrderItemMapper).insert(any(PurchaseOrderItem.class));
        verify(purchaseRequestMapper).updateById(any(PurchaseRequest.class));
    }

    @Test
    void testCreateOrderFromRequest_WithCustomItems() {
        // Given
        testRequest.setStatus("APPROVED");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestItemMapper.findByRequestId(1L)).thenReturn(List.of(testItem));
        when(purchaseOrderMapper.insert(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder o = invocation.getArgument(0);
            o.setId(10L);
            return 1;
        });
        when(purchaseOrderItemMapper.insert(any(PurchaseOrderItem.class))).thenReturn(1);
        when(purchaseRequestMapper.updateById(any(PurchaseRequest.class))).thenReturn(1);
        when(purchaseOrderMapper.selectCount(any())).thenReturn(0L);

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");

        // 自定义明细
        CreateOrderFromRequest.OrderItemInput customItem = new CreateOrderFromRequest.OrderItemInput();
        customItem.setRequestItemId(1L);
        customItem.setProductId(100L);
        customItem.setProductName("M8螺丝(定制)");
        customItem.setQuantity(new BigDecimal("50"));
        customItem.setUnitPrice(new BigDecimal("0.60"));
        params.setItems(List.of(customItem));

        // When
        PurchaseOrder result = purchaseRequestService.createOrderFromRequest(1L, params);

        // Then
        assertNotNull(result);
        verify(purchaseOrderItemMapper).insert(argThat(item ->
            "M8螺丝(定制)".equals(item.getProductName()) &&
            new BigDecimal("50").equals(item.getQuantity())
        ));
    }

    @Test
    void testCreateOrderFromRequest_NotApproved() {
        // Given
        testRequest.setStatus("PENDING"); // 未审批
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseRequestService.createOrderFromRequest(1L, params);
        });
    }

    @Test
    void testCreateOrderFromRequest_AlreadyHasOrder() {
        // Given
        testRequest.setStatus("APPROVED");
        testRequest.setPurchaseOrderId(100L); // 已生成订单
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseRequestService.createOrderFromRequest(1L, params);
        });
    }

    @Test
    void testCreateOrderFromRequest_EmptyItems() {
        // Given
        testRequest.setStatus("APPROVED");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestItemMapper.findByRequestId(1L)).thenReturn(List.of()); // 空明细

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseRequestService.createOrderFromRequest(1L, params);
        });
    }

    @Test
    void testCreateOrderFromRequest_RequestNotFound() {
        // Given
        when(purchaseRequestMapper.selectById(1L)).thenReturn(null);

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseRequestService.createOrderFromRequest(1L, params);
        });
    }

    @Test
    void testCreateOrderFromRequest_InvalidItem() {
        // Given
        testRequest.setStatus("APPROVED");
        when(purchaseRequestMapper.selectById(1L)).thenReturn(testRequest);
        when(purchaseRequestItemMapper.findByRequestId(1L)).thenReturn(List.of(testItem));

        CreateOrderFromRequest params = new CreateOrderFromRequest();
        params.setSupplierId(1L);
        params.setSupplierName("测试供应商");

        // 使用不存在的明细ID
        CreateOrderFromRequest.OrderItemInput invalidItem = new CreateOrderFromRequest.OrderItemInput();
        invalidItem.setRequestItemId(999L); // 不存在的ID
        params.setItems(List.of(invalidItem));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            purchaseRequestService.createOrderFromRequest(1L, params);
        });
    }
}
