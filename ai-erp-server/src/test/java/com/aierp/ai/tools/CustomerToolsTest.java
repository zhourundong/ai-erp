package com.aierp.ai.tools;

import com.aierp.entity.Customer;
import com.aierp.service.CustomerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 客户工具测试
 */
@ExtendWith(MockitoExtension.class)
class CustomerToolsTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerTools customerTools;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setCode("CUS001");
        testCustomer.setName("测试客户");
        testCustomer.setContactPerson("张三");
        testCustomer.setContactPhone("13800138000");
        testCustomer.setCreditLimit(BigDecimal.valueOf(100000));
        testCustomer.setCreditUsed(BigDecimal.ZERO);
        testCustomer.setStatus("ACTIVE");
    }

    @Test
    void testCreateCustomer_Success() throws Exception {
        // Given
        when(customerService.createCustomer(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(2L);
            c.setCode("CUS001");
            return c;
        });

        // When
        String result = customerTools.createCustomer(
                "新客户",
                "简称",
                "VIP",
                "李四",
                "13900139000",
                "test@example.com",
                "北京市",
                null, null, null,
                50000.0,
                "备注"
        );

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"id\":2"));
        assertTrue(result.contains("\"name\":\"新客户\""));

        verify(customerService).createCustomer(any(Customer.class));
    }

    @Test
    void testCreateCustomer_EmptyName() throws Exception {
        // When
        String result = customerTools.createCustomer(
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null, null, null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("客户名称不能为空"));

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void testCreateCustomer_NullName() throws Exception {
        // When
        String result = customerTools.createCustomer(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null, null, null,
                null,
                null
        );

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("客户名称不能为空"));
    }

    @Test
    void testSearchCustomers_Success() throws Exception {
        // Given
        Page<Customer> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testCustomer));
        mockPage.setTotal(1);

        when(customerService.pageCustomers(1, 10, "测试", "ACTIVE")).thenReturn(mockPage);

        // When
        String result = customerTools.searchCustomers("测试", 1, 10);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":1"));
        assertTrue(result.contains("\"name\":\"测试客户\""));
    }

    @Test
    void testSearchCustomers_DefaultPaging() throws Exception {
        // Given
        Page<Customer> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(customerService.pageCustomers(1, 10, null, "ACTIVE")).thenReturn(mockPage);

        // When
        String result = customerTools.searchCustomers(null, null, null);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"total\":0"));
    }

    @Test
    void testGetCustomerDetail_Success() throws Exception {
        // Given
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // When
        String result = customerTools.getCustomerDetail(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"name\":\"测试客户\""));
        assertTrue(result.contains("\"contactPerson\":\"张三\""));
    }

    @Test
    void testGetCustomerDetail_NotFound() throws Exception {
        // Given
        when(customerService.getById(999L)).thenReturn(null);

        // When
        String result = customerTools.getCustomerDetail(999L);

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("客户不存在"));
    }

    @Test
    void testCheckCustomerCredit_Success() throws Exception {
        // Given
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // When
        String result = customerTools.checkCustomerCredit(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"creditLimit\":100000"));
        assertTrue(result.contains("\"availableCredit\":100000"));
        assertTrue(result.contains("信用额度正常"));
    }

    @Test
    void testCheckCustomerCredit_HighUsage() throws Exception {
        // Given
        testCustomer.setCreditUsed(BigDecimal.valueOf(85000));
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // When
        String result = customerTools.checkCustomerCredit(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("信用额度使用率较高"));
    }

    @Test
    void testCheckCustomerCredit_Exhausted() throws Exception {
        // Given
        testCustomer.setCreditUsed(BigDecimal.valueOf(100000));
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // When
        String result = customerTools.checkCustomerCredit(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("信用额度已用完"));
    }

    @Test
    void testCheckCustomerCredit_NoLimit() throws Exception {
        // Given
        testCustomer.setCreditLimit(BigDecimal.ZERO);
        testCustomer.setCreditUsed(BigDecimal.ZERO);
        when(customerService.getById(1L)).thenReturn(testCustomer);

        // When
        String result = customerTools.checkCustomerCredit(1L);

        // Then
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("未设置信用额度"));
    }

    @Test
    void testCheckCustomerCredit_NotFound() throws Exception {
        // Given
        when(customerService.getById(999L)).thenReturn(null);

        // When
        String result = customerTools.checkCustomerCredit(999L);

        // Then
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("客户不存在"));
    }
}
