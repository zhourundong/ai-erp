package com.aierp.config;

import com.aierp.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * 从请求头中提取 JWT Token，解析并设置用户信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头获取 Token
        String token = extractToken(request);

        if (token != null) {
            try {
                // 解析 Token
                String username = jwtConfig.extractUsername(token);
                Long userId = jwtConfig.extractUserId(token);
                String role = jwtConfig.extractRole(token);

                if (userId != null && !jwtConfig.isTokenExpired(token)) {
                    // 设置用户信息到请求属性
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    request.setAttribute("role", role);

                    // 设置用户上下文（用于 Tool 调用）
                    UserContext userContext = new UserContext();
                    userContext.setUserId(userId);
                    userContext.setUsername(username);
                    UserContext.set(userContext);

                    log.debug("JWT认证成功: userId={}, username={}", userId, username);
                }
            } catch (Exception e) {
                log.debug("JWT解析失败: {}", e.getMessage());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清理用户上下文
            UserContext.clear();
        }
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
