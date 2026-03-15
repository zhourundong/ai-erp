# AI-ERP - AI原生企业资源规划系统

<div align="center">

![AI-ERP Logo](https://img.shields.io/badge/AI--ERP-v0.0.1-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen)
![React](https://img.shields.io/badge/React-18.2-61dafb)
![TypeScript](https://img.shields.io/badge/TypeScript-5.3-3178c6)
![License](https://img.shields.io/badge/License-MIT-green)

**AI驱动的下一代企业管理系统**

[功能特性](#功能特性) • [快速开始](#快速开始) • [技术架构](#技术架构) • [配置说明](#配置说明)

</div>

---

## 项目简介

AI-ERP是一个**AI原生**的企业资源规划系统，将AI能力深度融入业务流程的每个环节。不同于传统ERP的"AI附加"模式，AI-ERP从架构设计之初就将AI作为核心基础设施，实现"AI自主执行 + 人工监督"的新型企业管理模式。

### 核心理念

| 传统ERP | AI原生ERP |
|---------|-----------|
| 菜单导航 | 自然语言交互 |
| 表单填写 | AI智能解析 |
| 按钮操作 | 语音/文字指令 |
| 固定流程 | 动态智能编排 |
| 人工决策 | AI辅助决策 |

---

## 功能特性

### AI核心能力

- **自然语言交互** - 通过对话完成所有业务操作
- **流式响应 (SSE)** - 实时返回AI思考过程和回复，支持长任务处理
- **工具智能调用** - AI可调用业务工具完成复杂任务
- **AI唤起GUI** - AI可通过对话触发页面导航、打开创建表单、查看详情
- **多模型支持** - 支持 OpenAI 兼容的 AI 模型（基于 LangChain4j）
- **会话记忆** - 支持多会话上下文记忆

### 采购供应链模块

- **采购申请** - 创建、查询、审批采购申请
- **采购订单** - 创建订单、提交审批、审批通过、收货入库
- **供应商管理** - 供应商信息维护

### 销售管理模块

- **销售订单** - 创建订单、确认（锁定库存）、发货出库、取消订单
- **客户管理** - 客户信息维护、信用额度管理

### 库存管理模块

- **库存查询** - 实时库存数量、可用库存
- **库存事务** - 入库、出库、调拨记录

### 基础数据管理

- **商品管理** - 商品信息、SKU编码、分类管理
- **仓库管理** - 仓库信息维护

### 组织用户管理

- **用户管理** - 支持多角色权限控制
- **组织架构** - 树形组织结构管理
- **认证授权** - JWT Token认证

---

## AI工具清单

AI助手可通过以下工具执行业务操作：

| 工具类 | 功能说明 |
|-------|---------|
| **PurchaseOrderTools** | 创建/查询采购订单、提交审批、审批通过、收货入库 |
| **SalesOrderTools** | 创建/查询销售订单、确认订单、发货出库、取消订单 |
| **InventoryTools** | 查询库存数量、可用库存、库存事务 |
| **ProductTools** | 查询商品信息、按SKU查询、商品列表 |
| **SupplierTools** | 查询供应商信息、供应商列表 |
| **CustomerTools** | 查询客户信息、客户列表、信用额度 |
| **WarehouseTools** | 查询仓库信息、仓库列表 |
| **NavigationTools** | 页面导航、打开创建表单、查看详情（实现AI唤起GUI） |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- Maven 3.8+

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-org/ai-erp.git
cd ai-erp
```

#### 2. 启动后端

```bash
cd ai-erp-server

# 配置AI模型API Key（必需）
export OPENAI_API_KEY=your-openai-api-key

# 启动服务
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

#### 3. 启动前端

```bash
cd ai-erp-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

#### 4. 访问系统

打开浏览器访问 `http://localhost:3000`

**默认账号**: `admin`
**默认密码**: `admin123`

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    前端层 (React + TypeScript)                │
│         对话式界面 + 传统管理界面 + 数据可视化                  │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API / SSE (流式)
┌──────────────────────────┴──────────────────────────────────┐
│                 后端层 (Spring Boot 3.5.11 单体应用)           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   Controller 层                       │    │
│  │   ChatController(AuthController/XXXController...)    │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                 AI Agent 编排层                       │    │
│  │            AgentOrchestrator + ErpAssistant          │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  AI 工具层                           │    │
│  │   PurchaseOrderTools/SalesOrderTools/InventoryTools  │    │
│  │   ProductTools/SupplierTools/CustomerTools/...       │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    Service 层                         │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │               数据访问层 (MyBatis-Plus)               │    │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────┐
│                      数据层 (SQLite)                         │
└─────────────────────────────────────────────────────────────┘
```

### 技术选型

| 层级 | 技术 | 版本 | 说明 |
|-----|------|------|------|
| 前端框架 | React + TypeScript | 18.2 | 现代化前端开发栈 |
| UI组件 | Ant Design | 5.x | 企业级UI组件库 |
| 状态管理 | Zustand | - | 轻量级状态管理 |
| 构建工具 | Vite | - | 快速构建工具 |
| 后端框架 | Spring Boot | 3.5.11 | 主流企业级Java框架 |
| ORM | MyBatis-Plus | 3.5.5 | 强大的MyBatis增强工具 |
| 数据库 | SQLite | 3.45.1 | 轻量级嵌入式数据库 |
| AI集成 | LangChain4j | 1.12.2 | AI应用开发框架 |
| 认证 | Spring Security + JWT | 0.12.5 | 用户认证和授权 |

---

## 项目结构

```
ai-erp/
├── ai-erp-server/                    # 后端服务
│   ├── src/main/java/com/aierp/
│   │   ├── AiErpApplication.java     # 启动类
│   │   ├── config/                   # 配置类
│   │   │   ├── AiModelConfig.java    # AI模型配置
│   │   │   ├── AiServiceConfig.java  # AI服务配置
│   │   │   ├── SecurityConfig.java   # 安全配置
│   │   │   └── JwtConfig.java        # JWT配置
│   │   ├── controller/               # 控制器
│   │   │   ├── AuthController.java   # 认证
│   │   │   ├── ChatController.java   # AI对话
│   │   │   ├── PurchaseOrderController.java
│   │   │   ├── SalesOrderController.java
│   │   │   ├── InventoryController.java
│   │   │   ├── ProductController.java
│   │   │   ├── SupplierController.java
│   │   │   ├── CustomerController.java
│   │   │   └── WarehouseController.java
│   │   ├── service/                  # 业务服务
│   │   │   ├── PurchaseOrderService.java
│   │   │   ├── SalesOrderService.java
│   │   │   ├── InventoryService.java
│   │   │   └── ...
│   │   ├── ai/                       # AI服务
│   │   │   ├── ErpAssistant.java     # AI助手接口
│   │   │   ├── dto/                  # AI DTO
│   │   │   └── tools/                # AI工具
│   │   │       ├── PurchaseOrderTools.java
│   │   │       ├── SalesOrderTools.java
│   │   │       ├── InventoryTools.java
│   │   │       ├── ProductTools.java
│   │   │       ├── SupplierTools.java
│   │   │       ├── CustomerTools.java
│   │   │       ├── WarehouseTools.java
│   │   │       └── NavigationTools.java
│   │   ├── agent/                    # AI Agent编排
│   │   │   └── AgentOrchestrator.java
│   │   ├── entity/                   # 实体类
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── dto/                      # DTO对象
│   │   ├── common/                   # 通用工具
│   │   └── context/                  # 上下文
│   │       └── UserContext.java
│   ├── src/main/resources/
│   │   └── application.yml           # 主配置
│   ├── src/test/                     # 单元测试 (18个测试文件)
│   └── pom.xml
│
├── ai-erp-web/                       # 前端应用
│   ├── src/
│   │   ├── main.tsx                  # 入口文件
│   │   ├── App.tsx                   # 根组件
│   │   ├── pages/                    # 页面组件
│   │   │   ├── LoginPage.tsx         # 登录页
│   │   │   ├── ChatPage.tsx          # AI对话页
│   │   │   ├── PurchaseRequestPage.tsx
│   │   │   ├── PurchaseOrderPage.tsx
│   │   │   ├── SalesOrderPage.tsx
│   │   │   ├── InventoryPage.tsx
│   │   │   ├── InventoryTransactionPage.tsx
│   │   │   ├── ProductPage.tsx
│   │   │   ├── SupplierPage.tsx
│   │   │   ├── CustomerPage.tsx
│   │   │   ├── WarehousePage.tsx
│   │   │   ├── UserManagePage.tsx
│   │   │   └── OrganizationPage.tsx
│   │   ├── layouts/                  # 布局组件
│   │   ├── components/               # 通用组件
│   │   ├── stores/                   # 状态管理
│   │   ├── services/                 # API服务
│   │   └── types/                    # TypeScript类型
│   └── package.json
│
├── docs/                             # 文档
│   └── architecture.md               # 架构设计文档
├── TODO.md                           # 任务清单
└── README.md                         # 项目说明
```

---

## 配置说明

### 后端配置

配置文件位于 `ai-erp-server/src/main/resources/application.yml`

```yaml
# 服务器配置
server:
  port: 8080

# 数据库配置（SQLite）
spring:
  datasource:
    url: jdbc:sqlite:./data/aierp.db
    driver-class-name: org.sqlite.JDBC

# AI模型配置
spring:
  ai:
    models:
      openai:
        base-url: ${OPENAI_BASE_URL:}
        api-key: ${OPENAI_API_KEY:}
        model: ${OPENAI_MODEL:gpt-4o}

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| `OPENAI_API_KEY` | OpenAI API密钥 | - |
| `OPENAI_BASE_URL` | API地址（可配置兼容服务） | - |
| `OPENAI_MODEL` | 模型名称 | gpt-4o |
| `JWT_SECRET` | JWT签名密钥 | ai-erp-jwt-secret-key |
| `SERVER_PORT` | 服务端口 | 8080 |

### 前端配置

前端代理配置位于 `ai-erp-web/vite.config.ts`

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

---

## API文档

### 认证接口

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### AI对话接口

#### 流式对话（推荐）

```http
POST /api/chat/stream
Authorization: Bearer {token}
Content-Type: application/json

{
  "message": "帮我创建一个采购订单，供应商ID是1，商品是M8螺丝100个",
  "sessionId": "session-123"
}
```

响应格式 (SSE):
- `event: thinking` - AI思考过程（Base64编码）
- `event: token` - 响应文本片段（Base64编码）
- `event: tool` - 工具执行信息
- `event: action` - 导航动作（AI唤起GUI）
- `event: complete` - 完成事件
- `event: error` - 错误事件

### 业务接口

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /api/users | 用户列表 |
| POST | /api/users | 创建用户 |
| GET | /api/suppliers | 供应商列表 |
| GET | /api/customers | 客户列表 |
| GET | /api/products | 商品列表 |
| GET | /api/warehouses | 仓库列表 |
| GET | /api/inventory | 库存列表 |
| GET | /api/purchase/orders | 采购订单列表 |
| POST | /api/purchase/orders | 创建采购订单 |
| GET | /api/sales/orders | 销售订单列表 |
| POST | /api/sales/orders | 创建销售订单 |

---

## 开发指南

### 运行测试

```bash
# 后端测试
cd ai-erp-server
mvn test                         # 运行所有测试
mvn test -Dtest=PurchaseOrderServiceTest  # 运行单个测试类

# 前端测试
cd ai-erp-web
npm run test
```

### 代码规范

- Java代码遵循Google Java Style
- TypeScript代码遵循ESLint规范
- 提交信息遵循Conventional Commits

### 分支管理

- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `bugfix/*` - 修复分支

---

## 路线图

> 详细任务清单请查看 [TODO.md](./TODO.md)

### v0.0.1 (MVP) - 已完成 ✅

- [x] 用户登录认证
- [x] AI对话交互（流式响应）
- [x] 采购订单管理（创建、审批、收货）
- [x] 销售订单管理（创建、确认、发货）
- [x] 库存管理
- [x] 商品管理
- [x] 供应商管理
- [x] 客户管理
- [x] 仓库管理
- [x] 用户管理
- [x] 组织管理

### v0.1.0 (AI能力完善) - 已完成 ✅

- [x] AI工具调用（8个业务工具）
- [x] AI唤起GUI机制
- [x] 思考过程展示
- [x] 多会话管理

### v0.2.0 (体验优化) - 计划中

- [ ] 工具调用可视化优化
- [ ] 多会话搜索与导出
- [ ] 错误重试机制

### v0.3.0 (智能增强) - 规划中

- [ ] 发票OCR识别
- [ ] 合同智能审核
- [ ] 企业知识库（RAG）

### v0.4.0 (扩展能力) - 规划中

- [ ] 多租户支持
- [ ] 移动端适配
- [ ] 数据大屏
- [ ] 集成财务模块

---

## 贡献指南

我们欢迎所有形式的贡献！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

---

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 联系方式

- 项目主页: https://github.com/your-org/ai-erp
- 问题反馈: https://github.com/your-org/ai-erp/issues
- 邮箱: support@ai-erp.com

---

<div align="center">

**如果这个项目对你有帮助，请给一个Star支持一下！**

Made with love by AI-ERP Team

</div>
