package com.aierp.controller;

import com.aierp.common.result.Result;
import com.aierp.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = authService.login(request.getUsername(), request.getPassword());
        return Result.success(result);
    }
    
    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo(@RequestAttribute("userId") Long userId) {
        Map<String, Object> result = authService.getUserInfo(userId);
        return Result.success(result);
    }
    
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

@Data
class LoginRequest {
    private String username;
    private String password;
}
