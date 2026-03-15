-- AI ERP 完整数据库Schema
-- SQLite

-- ========================================
-- 基础组织架构
-- ========================================

-- 组织表
CREATE TABLE IF NOT EXISTS organizations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    parent_id INTEGER,
    type VARCHAR(20) DEFAULT 'DEPARTMENT',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    organization_id INTEGER,
    role VARCHAR(20) DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    avatar VARCHAR(255),
    last_login_time DATETIME,
    last_login_ip VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- ========================================
-- 商品物料管理
-- ========================================

-- 商品分类表
CREATE TABLE IF NOT EXISTS product_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    parent_id INTEGER,
    level INTEGER DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 商品/物料表
CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    category_id INTEGER,
    category_name VARCHAR(100),
    brand VARCHAR(100),
    model VARCHAR(100),
    specification VARCHAR(200),
    unit VARCHAR(20) NOT NULL,
    cost_price DECIMAL(12,2) DEFAULT 0,
    sale_price DECIMAL(12,2) DEFAULT 0,
    market_price DECIMAL(12,2) DEFAULT 0,
    safety_stock INTEGER DEFAULT 0,
    max_stock INTEGER DEFAULT 0,
    min_order_qty INTEGER DEFAULT 1,
    barcode VARCHAR(50),
    image_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES product_categories(id)
);

-- ========================================
-- 供应商管理
-- ========================================

-- 供应商表
CREATE TABLE IF NOT EXISTS suppliers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    category VARCHAR(100),
    level VARCHAR(20) DEFAULT 'NORMAL',
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address TEXT,
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_number VARCHAR(50),
    credit_code VARCHAR(50),
    rating INTEGER DEFAULT 0,
    score DECIMAL(5,2) DEFAULT 0,
    transaction_count INTEGER DEFAULT 0,
    total_transaction_amount DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 供应商商品关联表
CREATE TABLE IF NOT EXISTS supplier_products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    supplier_sku VARCHAR(50),
    purchase_price DECIMAL(12,2) DEFAULT 0,
    min_order_qty INTEGER DEFAULT 1,
    lead_time INTEGER DEFAULT 7,
    is_preferred INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ========================================
-- 采购管理
-- ========================================

-- 采购申请表
CREATE TABLE IF NOT EXISTS purchase_requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    request_no VARCHAR(50) UNIQUE NOT NULL,
    request_date DATE NOT NULL,
    applicant_id INTEGER,
    applicant_name VARCHAR(50),
    department_id INTEGER,
    department_name VARCHAR(100),
    requirement_description TEXT,
    ai_parsed_result TEXT,
    ai_recommendation TEXT,
    ai_estimated_amount DECIMAL(15,2),
    total_amount DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    approver_id INTEGER,
    approver_name VARCHAR(50),
    approval_time DATETIME,
    approval_comment TEXT,
    purchase_order_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (applicant_id) REFERENCES users(id),
    FOREIGN KEY (department_id) REFERENCES organizations(id)
);

-- 采购申请明细表
CREATE TABLE IF NOT EXISTS purchase_request_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id INTEGER NOT NULL,
    product_id INTEGER,
    product_sku VARCHAR(50),
    product_name VARCHAR(200) NOT NULL,
    specification VARCHAR(200),
    category VARCHAR(100),
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    estimated_price DECIMAL(12,2) DEFAULT 0,
    estimated_amount DECIMAL(15,2) DEFAULT 0,
    required_date DATE,
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (request_id) REFERENCES purchase_requests(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 采购订单表
CREATE TABLE IF NOT EXISTS purchase_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    order_date DATE NOT NULL,
    request_id INTEGER,
    supplier_id INTEGER NOT NULL,
    supplier_name VARCHAR(100),
    total_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    pay_amount DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    payment_method VARCHAR(50),
    expected_delivery_date DATE,
    actual_delivery_date DATE,
    buyer_id INTEGER,
    buyer_name VARCHAR(50),
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (request_id) REFERENCES purchase_requests(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- 采购订单明细表
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    product_id INTEGER,
    product_sku VARCHAR(50),
    product_name VARCHAR(200) NOT NULL,
    specification VARCHAR(200),
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    unit_price DECIMAL(12,2) DEFAULT 0,
    amount DECIMAL(15,2) DEFAULT 0,
    tax_rate DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    received_qty DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES purchase_orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ========================================
-- 销售管理
-- ========================================

-- 客户表
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    level VARCHAR(20) DEFAULT 'NORMAL',
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address TEXT,
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_number VARCHAR(50),
    credit_limit DECIMAL(15,2) DEFAULT 0,
    credit_used DECIMAL(15,2) DEFAULT 0,
    rating INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 销售订单表
CREATE TABLE IF NOT EXISTS sales_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    order_date DATE NOT NULL,
    customer_id INTEGER NOT NULL,
    customer_name VARCHAR(100),
    total_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    pay_amount DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    payment_method VARCHAR(50),
    expected_delivery_date DATE,
    actual_delivery_date DATE,
    sales_id INTEGER,
    sales_name VARCHAR(50),
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (sales_id) REFERENCES users(id)
);

-- 销售订单明细表
CREATE TABLE IF NOT EXISTS sales_order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    product_id INTEGER,
    product_sku VARCHAR(50),
    product_name VARCHAR(200) NOT NULL,
    specification VARCHAR(200),
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    unit_price DECIMAL(12,2) DEFAULT 0,
    amount DECIMAL(15,2) DEFAULT 0,
    tax_rate DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    shipped_qty DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES sales_orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ========================================
-- 库存管理
-- ========================================

-- 仓库表
CREATE TABLE IF NOT EXISTS warehouses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) DEFAULT 'NORMAL',
    address TEXT,
    manager VARCHAR(50),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 库存表
CREATE TABLE IF NOT EXISTS inventories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    warehouse_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    product_sku VARCHAR(50),
    product_name VARCHAR(200),
    quantity DECIMAL(10,2) DEFAULT 0,
    available_qty DECIMAL(10,2) DEFAULT 0,
    locked_qty DECIMAL(10,2) DEFAULT 0,
    cost_price DECIMAL(12,2) DEFAULT 0,
    last_in_date DATETIME,
    last_out_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 库存流水表
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_no VARCHAR(50) UNIQUE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    warehouse_id INTEGER NOT NULL,
    warehouse_name VARCHAR(100),
    product_id INTEGER NOT NULL,
    product_sku VARCHAR(50),
    product_name VARCHAR(200),
    quantity DECIMAL(10,2) NOT NULL,
    before_qty DECIMAL(10,2) DEFAULT 0,
    after_qty DECIMAL(10,2) DEFAULT 0,
    unit_cost DECIMAL(12,2) DEFAULT 0,
    total_cost DECIMAL(15,2) DEFAULT 0,
    related_order_type VARCHAR(50),
    related_order_id INTEGER,
    related_order_no VARCHAR(50),
    operator VARCHAR(50),
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ========================================
-- 创建索引
-- ========================================

-- 用户相关索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_organization ON users(organization_id);

-- 商品相关索引
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

-- 供应商相关索引
CREATE INDEX IF NOT EXISTS idx_suppliers_code ON suppliers(code);
CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers(name);
CREATE INDEX IF NOT EXISTS idx_suppliers_status ON suppliers(status);
CREATE INDEX IF NOT EXISTS idx_suppliers_category ON suppliers(category);

-- 采购申请索引
CREATE INDEX IF NOT EXISTS idx_purchase_requests_no ON purchase_requests(request_no);
CREATE INDEX IF NOT EXISTS idx_purchase_requests_applicant ON purchase_requests(applicant_id);
CREATE INDEX IF NOT EXISTS idx_purchase_requests_status ON purchase_requests(status);
CREATE INDEX IF NOT EXISTS idx_purchase_requests_date ON purchase_requests(request_date);

-- 采购订单索引
CREATE INDEX IF NOT EXISTS idx_purchase_orders_no ON purchase_orders(order_no);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_status ON purchase_orders(status);

-- 销售订单索引
CREATE INDEX IF NOT EXISTS idx_sales_orders_no ON sales_orders(order_no);
CREATE INDEX IF NOT EXISTS idx_sales_orders_customer ON sales_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_orders_status ON sales_orders(status);

-- 客户索引
CREATE INDEX IF NOT EXISTS idx_customers_code ON customers(code);
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers(name);

-- 库存索引
CREATE INDEX IF NOT EXISTS idx_inventories_warehouse ON inventories(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventories_product ON inventories(product_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_inventories_unique ON inventories(warehouse_id, product_id, deleted);

-- ========================================
-- 初始化数据
-- ========================================

-- 初始化组织
INSERT INTO organizations (id, name, code, type, status)
SELECT 1, '系统管理部', 'ADMIN', 'DEPARTMENT', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM organizations WHERE id = 1);

INSERT INTO organizations (id, name, code, type, status)
SELECT 2, '采购部', 'PURCHASE', 'DEPARTMENT', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM organizations WHERE id = 2);

INSERT INTO organizations (id, name, code, type, status)
SELECT 3, '销售部', 'SALES', 'DEPARTMENT', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM organizations WHERE id = 3);

INSERT INTO organizations (id, name, code, type, status)
SELECT 4, '仓储部', 'WAREHOUSE', 'DEPARTMENT', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM organizations WHERE id = 4);

-- 初始化默认仓库
INSERT INTO warehouses (id, code, name, type, status)
SELECT 1, 'WH001', '主仓库', 'MAIN', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM warehouses WHERE id = 1);

-- 初始化商品分类
INSERT INTO product_categories (id, name, code, level, status)
SELECT 1, '办公用品', 'OFFICE', 1, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE id = 1);

INSERT INTO product_categories (id, name, code, level, status)
SELECT 2, '电子设备', 'ELECTRONIC', 1, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE id = 2);

INSERT INTO product_categories (id, name, code, level, status)
SELECT 3, '原材料', 'MATERIAL', 1, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE id = 3);

-- 初始化示例供应商
INSERT INTO suppliers (id, code, name, category, contact_person, contact_phone, status)
SELECT 1, 'SUP001', '华为技术有限公司', '电子设备', '张经理', '13800138001', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE id = 1);

INSERT INTO suppliers (id, code, name, category, contact_person, contact_phone, status)
SELECT 2, 'SUP002', '得力集团', '办公用品', '李经理', '13800138002', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE id = 2);

-- 初始化示例客户
INSERT INTO customers (id, code, name, contact_person, contact_phone, status)
SELECT 1, 'CUS001', '阿里巴巴集团', '王经理', '13900139001', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE id = 1);

INSERT INTO customers (id, code, name, contact_person, contact_phone, status)
SELECT 2, 'CUS002', '腾讯科技', '赵经理', '13900139002', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE id = 2);

-- 初始化示例商品
INSERT INTO products (id, sku, name, category_id, category_name, unit, cost_price, sale_price, status)
SELECT 1, 'SKU001', 'A4打印纸', 1, '办公用品', '包', 25.00, 35.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 1);

INSERT INTO products (id, sku, name, category_id, category_name, unit, cost_price, sale_price, status)
SELECT 2, 'SKU002', '签字笔', 1, '办公用品', '盒', 15.00, 22.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 2);

INSERT INTO products (id, sku, name, category_id, category_name, unit, cost_price, sale_price, status)
SELECT 3, 'SKU003', '笔记本电脑', 2, '电子设备', '台', 4500.00, 5500.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 3);
