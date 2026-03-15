package com.aierp.controller;

import com.aierp.dto.LoginRequest;
import com.aierp.dto.LoginResponse;
import com.aierp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 提供用户登录和Token验证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含Token和用户信息）
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        LoginResponse response = authService.login(request, clientIp);
        return ResponseEntity.ok(response);
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }

        String token = authHeader.substring(7);
        boolean valid = authService.validateToken(token);
        return ResponseEntity.ok(valid);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UserInfo> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        var user = authService.getUserFromToken(token);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(LoginResponse.fromUser(user));
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
