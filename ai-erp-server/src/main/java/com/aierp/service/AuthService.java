package com.aierp.service;

import com.aierp.config.JwtConfig;
import com.aierp.dto.LoginRequest;
import com.aierp.dto.LoginResponse;
import com.aierp.entity.User;
import com.aierp.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;

    /**
     * 初始化管理员账户
     */
    @PostConstruct
    @Transactional
    public void initAdminUser() {
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setOrganizationId(1L); // 关联到系统管理部
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            userMapper.insert(admin);
            log.info("管理员账户初始化完成: admin/admin123");
        }
    }

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("用户已停用");
        }

        // 更新最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userMapper.updateById(user);

        // 生成Token
        String token = jwtConfig.generateToken(user.getUsername(), user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .expiresIn(86400L)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtConfig.extractUsername(token);
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return false;
            }
            return jwtConfig.validateToken(token, username);
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return false;
        }
    }

    /**
     * 根据Token获取用户信息
     */
    public User getUserFromToken(String token) {
        String username = jwtConfig.extractUsername(token);
        return userMapper.findByUsername(username);
    }
}
