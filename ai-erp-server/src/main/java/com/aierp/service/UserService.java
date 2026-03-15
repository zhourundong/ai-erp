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
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

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
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(user.getStatus() == null ? "ACTIVE" : user.getStatus());
        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        // 如果修改了密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existing.getPassword());
        }
        userMapper.updateById(user);
        return user;
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 分页查询用户
     */
    public Page<User> pageUsers(int pageNum, int pageSize, String keyword, String status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getEmail, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(page, wrapper);
    }

    /**
     * 获取用户详情
     */
    public User getUserById(Long id) {
        return userMapper.findByIdWithOrg(id);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}
