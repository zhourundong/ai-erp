package com.aierp.service;

import com.aierp.common.exception.BusinessException;
import com.aierp.common.utils.JwtUtil;
import com.aierp.entity.User;
import com.aierp.entity.Role;
import com.aierp.entity.Menu;
import com.aierp.mapper.UserMapper;
import com.aierp.mapper.UserRoleMapper;
import com.aierp.mapper.RoleMenuMapper;
import com.aierp.mapper.MenuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    public Map<String, Object> login(String username, String password) {
        // 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0)
        );
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 获取用户角色
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        
        // 获取用户权限
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByUserId(user.getId());
        List<Menu> menus = menuMapper.selectBatchIds(menuIds);
        List<String> permissions = menus.stream()
            .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty())
            .map(Menu::getPermission)
            .collect(Collectors.toList());
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("roles", roleIds);
        userInfo.put("permissions", permissions);
        result.put("user", userInfo);
        
        return result;
    }
    
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByUserId(userId);
        List<Menu> menus = menuMapper.selectBatchIds(menuIds);
        
        // 构建菜单树
        List<Map<String, Object>> menuTree = buildMenuTree(menus, 0L);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("avatar", user.getAvatar());
        result.put("roles", roleIds);
        result.put("menus", menuTree);
        
        return result;
    }
    
    private List<Map<String, Object>> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
            .filter(m -> parentId.equals(m.getParentId()))
            .map(m -> {
                Map<String, Object> node = new HashMap<>();
                node.put("id", m.getId());
                node.put("name", m.getMenuName());
                node.put("path", m.getPath());
                node.put("icon", m.getIcon());
                node.put("component", m.getComponent());
                node.put("children", buildMenuTree(menus, m.getId()));
                return node;
            })
            .collect(Collectors.toList());
    }
}
