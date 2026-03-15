package com.aierp.dto;

import com.aierp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token类型
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 过期时间(秒)
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String department;
        private String role;
    }

    /**
     * 从User实体构建UserInfo
     */
    public static UserInfo fromUser(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
