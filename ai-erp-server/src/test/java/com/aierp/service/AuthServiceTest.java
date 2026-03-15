package com.aierp.service;

import com.aierp.dto.LoginRequest;
import com.aierp.dto.LoginResponse;
import com.aierp.entity.User;
import com.aierp.mapper.UserMapper;
import com.aierp.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtConfig jwtConfig;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRealName("测试用户");
        testUser.setRole("USER");
        testUser.setStatus("ACTIVE");
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(jwtConfig.generateToken(eq("testuser"), eq(1L), eq("USER"))).thenReturn("mock-jwt-token");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        LoginResponse response = authService.login(request, "127.0.0.1");

        // Then
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void testLogin_UserInactive() {
        // Given
        testUser.setStatus("INACTIVE");
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void testValidateToken_Success() {
        // Given
        when(jwtConfig.extractUsername("valid-token")).thenReturn("testuser");
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(jwtConfig.validateToken("valid-token", "testuser")).thenReturn(true);

        // When
        boolean result = authService.validateToken("valid-token");

        // Then
        assertTrue(result);
    }

    @Test
    void testGetUserFromToken() {
        // Given
        when(jwtConfig.extractUsername("valid-token")).thenReturn("testuser");
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // When
        User user = authService.getUserFromToken("valid-token");

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
}
