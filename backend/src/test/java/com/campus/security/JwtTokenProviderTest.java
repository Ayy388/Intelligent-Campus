package com.campus.security;

import com.campus.common.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "this-is-a-test-secret-key-that-is-at-least-32-bytes-long-for-hs256";
    private static final long EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("生成 token 并解析成功")
    void testGenerateAndParseToken_shouldSucceed() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        String role = "student";

        // Act
        String token = jwtTokenProvider.generateToken(userId, username, role);
        Claims claims = jwtTokenProvider.parseToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(username, claims.get("username"));
        assertEquals(role, claims.get("role"));
    }

    @Test
    @DisplayName("过期 token 应抛出异常")
    void testParseExpiredToken_shouldThrowException() throws InterruptedException {
        // Arrange
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(SECRET, 1L); // 1 ms expiration
        String token = shortLivedProvider.generateToken(1L, "user", "student");

        // Wait for token to expire
        Thread.sleep(10);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> shortLivedProvider.parseToken(token));
    }

    @Test
    @DisplayName("无效 token 应返回 false")
    void testValidateInvalidToken_shouldReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    @DisplayName("有效 token 应返回 true")
    void testValidateValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken(1L, "user", "admin");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("从 token 中提取 userId 成功")
    void testGetUserId_shouldReturnCorrectId() {
        String token = jwtTokenProvider.generateToken(42L, "john", "teacher");
        Long userId = jwtTokenProvider.getUserId(token);
        assertEquals(42L, userId);
    }
}