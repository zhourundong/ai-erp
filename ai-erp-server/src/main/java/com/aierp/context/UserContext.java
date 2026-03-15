package com.aierp.context;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户上下文
 *
 * 用于在请求处理过程中存储和获取当前登录用户信息
 * 支持跨线程传递（如异步处理、AI模型回调等）
 */
@Data
public class UserContext {

    private Long userId;
    private String username;
    private String realName;
    private Long departmentId;
    private String departmentName;

    // 使用 ThreadLocal 支持同步请求
    private static final ThreadLocal<UserContext> THREAD_LOCAL = new ThreadLocal<>();

    // 使用 ConcurrentHashMap 支持跨线程传递（key: sessionId）
    private static final ConcurrentHashMap<String, UserContext> SESSION_CONTEXT = new ConcurrentHashMap<>();

    /**
     * 设置当前线程的用户上下文
     */
    public static void set(UserContext context) {
        THREAD_LOCAL.set(context);
    }

    /**
     * 获取当前线程的用户上下文
     */
    public static UserContext get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 清除当前线程的用户上下文
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }

    /**
     * 设置会话的用户上下文（用于跨线程传递）
     */
    public static void setForSession(String sessionId, UserContext context) {
        if (sessionId != null && context != null) {
            SESSION_CONTEXT.put(sessionId, context);
        }
    }

    /**
     * 获取会话的用户上下文
     */
    public static UserContext getForSession(String sessionId) {
        if (sessionId == null) return null;
        return SESSION_CONTEXT.get(sessionId);
    }

    /**
     * 清除会话的用户上下文
     */
    public static void clearForSession(String sessionId) {
        if (sessionId != null) {
            SESSION_CONTEXT.remove(sessionId);
        }
    }

    /**
     * 判断是否有登录用户
     */
    public static boolean isLoggedIn() {
        return THREAD_LOCAL.get() != null && THREAD_LOCAL.get().getUserId() != null;
    }
}
