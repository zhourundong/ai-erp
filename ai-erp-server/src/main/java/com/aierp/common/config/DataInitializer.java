package com.aierp.common.config;

import com.aierp.entity.User;
import com.aierp.entity.Role;
import com.aierp.entity.Menu;
import com.aierp.entity.UserRole;
import com.aierp.entity.RoleMenu;
import com.aierp.mapper.UserMapper;
import com.aierp.mapper.RoleMapper;
import com.aierp.mapper.MenuMapper;
import com.aierp.mapper.UserRoleMapper;
import com.aierp.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) {
        log.info("开始初始化数据...");
        
        // 创建表结构
        createTables();
        
        // 初始化菜单
        initMenus();
        
        // 初始化角色
        Role adminRole = initRoles();
        
        // 初始化管理员
        User admin = initAdmin();
        
        // 关联用户角色
        initUserRole(admin, adminRole);
        
        // 关联角色菜单
        initRoleMenu(adminRole);
        
        log.info("数据初始化完成!");
    }
    
    private void createTables() {
        try {
            // 创建用户表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "username VARCHAR(50) NOT NULL," +
                "password VARCHAR(200) NOT NULL," +
                "real_name VARCHAR(50) NOT NULL," +
                "phone VARCHAR(20)," +
                "email VARCHAR(100)," +
                "dept_id INTEGER," +
                "avatar VARCHAR(500)," +
                "status INTEGER DEFAULT 1," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "deleted INTEGER DEFAULT 0" +
                ")");
            
            // 创建角色表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "role_name VARCHAR(50) NOT NULL," +
                "role_code VARCHAR(50) NOT NULL," +
                "description VARCHAR(200)," +
                "data_scope VARCHAR(20) DEFAULT 'self'," +
                "status INTEGER DEFAULT 1," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "deleted INTEGER DEFAULT 0" +
                ")");
            
            // 创建菜单表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_menu (" +
                "id INTEGER PRIMARY KEY," +
                "parent_id INTEGER DEFAULT 0," +
                "menu_name VARCHAR(50) NOT NULL," +
                "menu_code VARCHAR(50)," +
                "menu_type INTEGER NOT NULL," +
                "path VARCHAR(200)," +
                "component VARCHAR(200)," +
                "permission VARCHAR(100)," +
                "icon VARCHAR(100)," +
                "sort INTEGER DEFAULT 0," +
                "status INTEGER DEFAULT 1," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // 创建用户角色关联表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_role (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "user_id INTEGER NOT NULL," +
                "role_id INTEGER NOT NULL," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // 创建角色菜单关联表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role_menu (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "role_id INTEGER NOT NULL," +
                "menu_id INTEGER NOT NULL," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // 创建商品表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS product (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "product_code VARCHAR(50)," +
                "product_name VARCHAR(200) NOT NULL," +
                "category_id INTEGER," +
                "brand VARCHAR(100)," +
                "unit VARCHAR(20) NOT NULL," +
                "specification VARCHAR(200)," +
                "cost_price DECIMAL(18,4)," +
                "sale_price DECIMAL(18,4)," +
                "safety_stock_days INTEGER DEFAULT 30," +
                "purchase_lead_time INTEGER DEFAULT 7," +
                "status INTEGER DEFAULT 1," +
                "ai_prediction_enabled INTEGER DEFAULT 1," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "deleted INTEGER DEFAULT 0" +
                ")");
            
            // 创建供应商表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS supplier (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "supplier_code VARCHAR(50)," +
                "supplier_name VARCHAR(200) NOT NULL," +
                "contact_name VARCHAR(50)," +
                "contact_phone VARCHAR(20)," +
                "contact_email VARCHAR(100)," +
                "address VARCHAR(500)," +
                "score DECIMAL(5,2)," +
                "price_score DECIMAL(5,2)," +
                "delivery_score DECIMAL(5,2)," +
                "quality_score DECIMAL(5,2)," +
                "service_score DECIMAL(5,2)," +
                "on_time_rate DECIMAL(5,4)," +
                "quality_rate DECIMAL(5,4)," +
                "status INTEGER DEFAULT 1," +
                "remark VARCHAR(500)," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "deleted INTEGER DEFAULT 0" +
                ")");
            
            // 创建采购订单表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS purchase_order (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "order_no VARCHAR(50) NOT NULL," +
                "supplier_id INTEGER," +
                "expected_arrival_date DATETIME," +
                "warehouse_id INTEGER," +
                "total_amount DECIMAL(18,2)," +
                "status INTEGER DEFAULT 0," +
                "ai_suggestion TEXT," +
                "remark VARCHAR(500)," +
                "created_by INTEGER," +
                "approved_by INTEGER," +
                "approved_time DATETIME," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "deleted INTEGER DEFAULT 0" +
                ")");
            
            // 创建采购订单明细表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS purchase_order_item (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "order_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "product_name VARCHAR(200)," +
                "unit VARCHAR(20)," +
                "quantity DECIMAL(18,4)," +
                "unit_price DECIMAL(18,4)," +
                "amount DECIMAL(18,4)," +
                "received_quantity DECIMAL(18,4) DEFAULT 0," +
                "remark VARCHAR(500)," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // 创建库存表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL DEFAULT 1," +
                "product_id INTEGER NOT NULL," +
                "warehouse_id INTEGER," +
                "quantity DECIMAL(18,4) DEFAULT 0," +
                "available_quantity DECIMAL(18,4) DEFAULT 0," +
                "locked_quantity DECIMAL(18,4) DEFAULT 0," +
                "warning_threshold DECIMAL(18,4)," +
                "ai_warning_threshold DECIMAL(18,4)," +
                "ai_warning_status INTEGER DEFAULT 0," +
                "last_in_time DATETIME," +
                "last_out_time DATETIME," +
                "inventory_turnover_days INTEGER," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

            // 创建AI对话记录表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ai_conversation (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "session_id VARCHAR(50) NOT NULL," +
                "role VARCHAR(20) NOT NULL," +
                "content TEXT NOT NULL," +
                "model VARCHAR(50)," +
                "tokens_used INTEGER DEFAULT 0," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

            // 创建AI预测记录表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ai_prediction (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenant_id INTEGER NOT NULL," +
                "prediction_type VARCHAR(50) NOT NULL," +
                "target_type VARCHAR(50) NOT NULL," +
                "target_id INTEGER NOT NULL," +
                "prediction_date DATE NOT NULL," +
                "predicted_value DECIMAL(18,4) NOT NULL," +
                "confidence DECIMAL(5,4)," +
                "actual_value DECIMAL(18,4)," +
                "model VARCHAR(50)," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

            log.info("数据库表创建完成");
        } catch (Exception e) {
            log.error("创建表失败: {}", e.getMessage());
        }
    }
    
    private void initMenus() {
        Long count = menuMapper.selectCount(null);
        if (count > 0) return;
        
        List<Menu> menus = new ArrayList<>();
        
        // 系统管理
        menus.add(createMenu(1L, 0L, "系统管理", "system", 1, "/system", "SettingOutlined", 1));
        menus.add(createMenu(2L, 1L, "用户管理", "user", 2, "/system/user", "UserOutlined", 1));
        menus.add(createMenu(3L, 1L, "角色管理", "role", 2, "/system/role", "TeamOutlined", 2));
        menus.add(createMenu(4L, 1L, "菜单管理", "menu", 2, "/system/menu", "MenuOutlined", 3));
        
        // 进销存
        menus.add(createMenu(10L, 0L, "进销存", "psm", 1, "/psm", "ShoppingOutlined", 2));
        menus.add(createMenu(11L, 10L, "采购管理", "purchase", 2, "/psm/purchase", null, 1));
        menus.add(createMenu(12L, 10L, "库存管理", "inventory", 2, "/psm/inventory", null, 2));
        menus.add(createMenu(13L, 10L, "销售管理", "sales", 2, "/psm/sales", null, 3));
        
        // AI 助手
        menus.add(createMenu(20L, 0L, "AI 助手", "ai", 1, "/ai", "RobotOutlined", 3));
        menus.add(createMenu(21L, 20L, "智能对话", "chat", 2, "/ai/chat", null, 1));
        menus.add(createMenu(22L, 20L, "预测分析", "predict", 2, "/ai/predict", null, 2));
        
        // 财务管理
        menus.add(createMenu(30L, 0L, "财务管理", "finance", 1, "/finance", "AccountBookOutlined", 4));
        menus.add(createMenu(31L, 30L, "凭证管理", "voucher", 2, "/finance/voucher", null, 1));
        menus.add(createMenu(32L, 30L, "财务报表", "report", 2, "/finance/report", null, 2));
        
        // 人事管理
        menus.add(createMenu(40L, 0L, "人事管理", "hr", 1, "/hr", "IdcardOutlined", 5));
        menus.add(createMenu(41L, 40L, "员工管理", "employee", 2, "/hr/employee", null, 1));
        menus.add(createMenu(42L, 40L, "考勤管理", "attendance", 2, "/hr/attendance", null, 2));
        
        menus.forEach(menuMapper::insert);
        log.info("菜单初始化完成");
    }
    
    private Menu createMenu(Long id, Long parentId, String name, String code, Integer type, String path, String icon, Integer sort) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuCode(code);
        menu.setMenuType(type);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setStatus(1);
        return menu;
    }
    
    private Role initRoles() {
        Long count = roleMapper.selectCount(null);
        if (count > 0) {
            return roleMapper.selectOne(null);
        }
        
        Role role = new Role();
        role.setTenantId(1L);
        role.setRoleName("超级管理员");
        role.setRoleCode("admin");
        role.setDescription("系统超级管理员");
        role.setDataScope("all");
        role.setStatus(1);
        roleMapper.insert(role);
        
        log.info("角色初始化完成");
        return role;
    }
    
    private User initAdmin() {
        Long count = userMapper.selectCount(null);
        if (count > 0) {
            return userMapper.selectOne(null);
        }
        
        User user = new User();
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRealName("管理员");
        user.setPhone("13800138000");
        user.setEmail("admin@aierp.com");
        user.setStatus(1);
        userMapper.insert(user);
        
        log.info("管理员初始化完成 - 用户名: admin, 密码: admin123");
        return user;
    }
    
    private void initUserRole(User user, Role role) {
        Long count = userRoleMapper.selectCount(null);
        if (count > 0) return;
        
        UserRole userRole = new UserRole();
        userRole.setTenantId(1L);
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);
        
        log.info("用户角色关联完成");
    }
    
    private void initRoleMenu(Role role) {
        Long count = roleMenuMapper.selectCount(null);
        if (count > 0) return;
        
        // 关联所有菜单
        List<Menu> menus = menuMapper.selectList(null);
        for (Menu menu : menus) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setTenantId(1L);
            roleMenu.setRoleId(role.getId());
            roleMenu.setMenuId(menu.getId());
            roleMenuMapper.insert(roleMenu);
        }
        
        log.info("角色菜单关联完成");
    }
}
