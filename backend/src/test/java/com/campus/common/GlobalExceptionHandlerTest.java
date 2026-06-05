package com.campus.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException 返回 Result.error 格式")
    void testHandleBusinessException_shouldReturnErrorResult() {
        // Arrange
        BusinessException ex = new BusinessException(400, "业务异常信息");

        // Act
        Result<Void> result = handler.handleBusiness(ex);

        // Assert
        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("业务异常信息", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("BusinessException 默认 code 为 500")
    void testHandleBusinessExceptionWithDefaultCode_shouldReturn500() {
        // Arrange
        BusinessException ex = new BusinessException("默认错误");

        // Act
        Result<Void> result = handler.handleBusiness(ex);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("默认错误", result.getMessage());
    }

    @Test
    @DisplayName("通用 Exception 返回 500 服务器内部错误")
    void testHandleGenericException_shouldReturn500() {
        // Arrange
        Exception ex = new RuntimeException("未知异常");

        // Act
        Result<Void> result = handler.handleOther(ex);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("服务器内部错误"));
        assertTrue(result.getMessage().contains("未知异常"));
    }

    @Test
    @DisplayName("Result.ok() 返回正确格式")
    void testResultOk_shouldReturnSuccess() {
        // Act
        Result<String> result = Result.ok("data");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("Result.error() 返回错误格式")
    void testResultError_shouldReturnError() {
        // Act
        Result<Void> result = Result.error(403, "禁止访问");

        // Assert
        assertEquals(403, result.getCode());
        assertEquals("禁止访问", result.getMessage());
    }
}