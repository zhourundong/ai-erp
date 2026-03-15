package com.aierp.service;

import com.aierp.entity.Customer;
import com.aierp.mapper.CustomerMapper;
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
 * 客户服务测试
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setCode("CUS001");
        testCustomer.setName("测试客户");
        testCustomer.setContactPerson("张三");
        testCustomer.setContactPhone("13800138000");
        testCustomer.setStatus("ACTIVE");
        testCustomer.setCreditLimit(new BigDecimal("100000"));
        testCustomer.setCreditUsed(BigDecimal.ZERO);
    }

    @Test
    void testCreateCustomer_Success() {
        // Given
        Customer newCustomer = new Customer();
        newCustomer.setName("新客户");

        when(customerMapper.insert(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(2L);
            return 1;
        });

        // When
        Customer created = customerService.createCustomer(newCustomer);

        // Then
        assertNotNull(created);
        assertNotNull(created.getCode());
        assertEquals("ACTIVE", created.getStatus());
        verify(customerMapper).insert(any(Customer.class));
    }

    @Test
    void testPageCustomers() {
        // Given
        Page<Customer> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testCustomer));
        mockPage.setTotal(1);

        when(customerMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<Customer> result = customerService.pageCustomers(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testUpdateCustomer() {
        // Given
        testCustomer.setName("更新后的客户");
        when(customerMapper.updateById(any(Customer.class))).thenReturn(1);

        // When
        Customer updated = customerService.updateCustomer(testCustomer);

        // Then
        assertEquals("更新后的客户", updated.getName());
        verify(customerMapper).updateById(any(Customer.class));
    }

    @Test
    void testDeleteCustomer() {
        // Given
        when(customerMapper.deleteById(1L)).thenReturn(1);

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerMapper).deleteById(1L);
    }
}
