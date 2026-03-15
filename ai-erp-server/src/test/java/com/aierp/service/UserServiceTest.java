package com.aierp.service;

import com.aierp.config.JwtConfig;
import com.aierp.dto.LoginRequest;
import com.aierp.dto.LoginResponse;
import com.aierp.entity.User;
import com.aierp.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password");
        testUser.setRealName("Test User");
        testUser.setRole("BUYER");
        testUser.setStatus("ACTIVE");
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        when(jwtConfig.generateToken(anyString(), anyLong(), anyString())).thenReturn("test-token");

        // When
        LoginResponse response = userService.login(request, "127.0.0.1");

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
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
        assertThrows(RuntimeException.class, () -> {
            userService.login(request, "127.0.0.1");
        });
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(request, "127.0.0.1");
        });
    }

    @Test
    void testLogin_UserInactive() {
        // Given
        testUser.setStatus("INACTIVE");
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(request, "127.0.0.1");
        });
    }

    @Test
    void testCreateUser_Success() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        newUser.setRealName("New User");

        when(userMapper.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return 1;
        });

        // When
        User created = userService.createUser(newUser);

        // Then
        assertNotNull(created);
        assertEquals("encoded_password", created.getPassword());
        assertEquals("ACTIVE", created.getStatus());

        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        // Given
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("password");

        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(newUser);
        });
    }

    @Test
    void testPageUsers() {
        // Given
        Page<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testUser));
        mockPage.setTotal(1);

        when(userMapper.selectPage(any(), any())).thenReturn(mockPage);

        // When
        Page<User> result = userService.pageUsers(1, 10, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }
}
