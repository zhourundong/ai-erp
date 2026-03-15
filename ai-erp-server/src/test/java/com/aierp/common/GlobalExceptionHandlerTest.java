package com.aierp.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        // Given
        BusinessException exception = new BusinessException("库存不足");

        // When
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("库存不足", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessException_WithCode() {
        // Given
        BusinessException exception = new BusinessException(404, "订单不存在");

        // When
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("订单不存在", response.getBody().getMessage());
    }

    @Test
    void testHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("未知错误");

        // When
        ResponseEntity<Result<Void>> response = handler.handleRuntimeException(exception);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("未知错误", response.getBody().getMessage());
    }

    @Test
    void testHandleException() {
        // Given
        Exception exception = new Exception("系统错误");

        // When
        ResponseEntity<Result<Void>> response = handler.handleException(exception);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("系统错误，请稍后重试", response.getBody().getMessage());
    }
}
