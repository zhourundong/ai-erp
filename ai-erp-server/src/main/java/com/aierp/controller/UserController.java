package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.User;
import com.aierp.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    public ResponseEntity<Result<Page<User>>> pageUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<User> page = userService.pageUsers(pageNum, pageSize, keyword, status);
        return ResponseEntity.ok(Result.success(page));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<User>> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(Result.success(user));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<Result<User>> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(Result.success(created));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updated = userService.updateUser(user);
        return ResponseEntity.ok(Result.success(updated));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Result.success());
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Result.success());
    }
}
