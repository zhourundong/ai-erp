package com.aierp.context;

import com.aierp.entity.Organization;
import com.aierp.entity.User;
import com.aierp.service.OrganizationService;
import com.aierp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 *
 * 从请求属性中获取已认证的用户信息，设置到 UserContext 中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final OrganizationService organizationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求属性中获取用户ID（由 JwtConfig 设置）
        Object userIdAttr = request.getAttribute("userId");

        if (userIdAttr != null) {
            try {
                Long userId = Long.valueOf(userIdAttr.toString());
                User user = userService.getById(userId);

                if (user != null) {
                    UserContext context = new UserContext();
                    context.setUserId(user.getId());
                    context.setUsername(user.getUsername());
                    context.setRealName(user.getRealName());
                    context.setDepartmentId(user.getOrganizationId());

                    // 获取部门名称
                    if (user.getOrganizationId() != null) {
                        Organization org = organizationService.getById(user.getOrganizationId());
                        if (org != null) {
                            context.setDepartmentName(org.getName());
                        }
                    }

                    UserContext.set(context);
                    log.debug("设置用户上下文: userId={}, username={}", userId, user.getUsername());
                }
            } catch (Exception e) {
                log.warn("设置用户上下文失败: {}", e.getMessage());
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文
        UserContext.clear();
    }
}
