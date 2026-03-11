-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL DEFAULT 1,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    dept_id INTEGER,
    avatar VARCHAR(500),
    status INTEGER DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_username ON sys_user(tenant_id, username);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL DEFAULT 1,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    data_scope VARCHAR(20) DEFAULT 'self',
    status INTEGER DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id INTEGER PRIMARY KEY,
    parent_id INTEGER DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_code VARCHAR(50),
    menu_type INTEGER NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    permission VARCHAR(100),
    icon VARCHAR(100),
    sort INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL DEFAULT 1,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_role ON sys_user_role(user_id, role_id);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL DEFAULT 1,
    role_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_role_menu ON sys_role_menu(role_id, menu_id);
