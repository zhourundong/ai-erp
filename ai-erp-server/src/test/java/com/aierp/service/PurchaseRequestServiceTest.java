package com.aierp.service;

import com.aierp.entity.PurchaseRequest;
import com.aierp.entity.PurchaseRequestItem;
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

    @InjectMocks
    private PurchaseRequestService purchaseRequestService;

    private PurchaseRequest testRequest;

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
        assertThrows(RuntimeException.class, () -> {
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
}
