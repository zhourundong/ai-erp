# AI-ERP - AI原生企业资源规划系统

<div align="center">

![AI-ERP Logo](https://img.shields.io/badge/AI--ERP-v0.0.1-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)
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

### 🤖 AI核心能力

- **自然语言交互** - 通过对话完成所有业务操作
- **意图智能识别** - 自动理解用户需求并规划执行
- **多模型支持** - 支持Claude、OpenAI等主流AI模型
- **Skills渐进式加载** - 按需加载AI技能，优化性能
- **工具智能调用** - AI可调用业务工具完成复杂任务

### 📦 采购供应链模块

- **智能采购申请** - 自然语言描述需求，AI自动生成结构化申请
- **供应商智能推荐** - 基于历史数据多维度推荐最优供应商
- **采购订单管理** - 全流程跟踪，AI风险预警
- **审批流程** - 可配置的审批流程，支持移动端

### 👥 组织用户管理

- **用户管理** - 支持多角色权限控制
- **组织架构** - 树形组织结构管理
- **认证授权** - JWT Token认证

### 📊 数据分析

- **对话式查询** - 自然语言查询业务数据
- **智能洞察** - AI自动分析数据并生成洞察报告

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

# 配置AI模型API Key（可选，不配置可使用Mock模式）
export ANTHROPIC_API_KEY=your-claude-api-key
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
                           │ REST API / SSE
┌──────────────────────────┴──────────────────────────────────┐
│                 后端层 (Spring Boot 3.x 单体应用)             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   Controller 层                       │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    Service 层                         │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                 AI Agent 编排层                       │    │
│  │   AgentOrchestrator │ IntentClassifier │ TaskPlanner │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  AI 服务适配层                        │    │
│  │         ClaudeAdapter │ OpenAIAdapter │ ModelFactory │    │
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

| 层级 | 技术 | 说明 |
|-----|------|------|
| 前端框架 | React 18 + TypeScript | 现代化前端开发栈 |
| UI组件 | Ant Design 5 | 企业级UI组件库 |
| 状态管理 | Zustand | 轻量级状态管理 |
| 构建工具 | Vite | 快速构建工具 |
| 后端框架 | Spring Boot 3.x | 主流企业级Java框架 |
| ORM | MyBatis-Plus | 强大的MyBatis增强工具 |
| 数据库 | SQLite | 轻量级嵌入式数据库 |
| AI集成 | LangChain4j | AI应用开发框架 |
| 认证 | Spring Security + JWT | 用户认证和授权 |

---

## 项目结构

```
ai-erp/
├── ai-erp-server/                    # 后端服务
│   ├── src/main/java/com/aierp/
│   │   ├── AiErpApplication.java     # 启动类
│   │   ├── config/                   # 配置类
│   │   │   ├── MybatisPlusConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── AiModelConfig.java
│   │   │   └── JwtConfig.java
│   │   ├── controller/               # 控制器
│   │   │   ├── AuthController.java
│   │   │   ├── ChatController.java
│   │   │   ├── UserController.java
│   │   │   ├── SupplierController.java
│   │   │   └── PurchaseRequestController.java
│   │   ├── service/                  # 业务服务
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── entity/                   # 实体类
│   │   ├── agent/                    # AI Agent编排
│   │   │   ├── AgentOrchestrator.java
│   │   │   ├── IntentClassifier.java
│   │   │   └── TaskPlanner.java
│   │   ├── ai/                       # AI服务适配
│   │   │   ├── ModelAdapter.java
│   │   │   ├── ModelFactory.java
│   │   │   └── adapter/
│   │   │       ├── ClaudeAdapter.java
│   │   │       └── OpenAIAdapter.java
│   │   ├── dto/                      # DTO对象
│   │   └── common/                   # 通用工具
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置
│   │   └── application-dev.yml       # 开发配置
│   ├── src/test/                     # 单元测试
│   └── pom.xml
│
├── ai-erp-web/                       # 前端应用
│   ├── src/
│   │   ├── main.tsx                  # 入口文件
│   │   ├── App.tsx                   # 根组件
│   │   ├── pages/                    # 页面组件
│   │   │   ├── LoginPage.tsx
│   │   │   ├── ChatPage.tsx
│   │   │   ├── UserManagePage.tsx
│   │   │   ├── OrganizationPage.tsx
│   │   │   ├── SupplierPage.tsx
│   │   │   └── PurchaseRequestPage.tsx
│   │   ├── layouts/                  # 布局组件
│   │   ├── components/               # 通用组件
│   │   ├── stores/                   # 状态管理
│   │   ├── services/                 # API服务
│   │   └── types/                    # TypeScript类型
│   ├── package.json
│   └── vite.config.ts
│
├── docs/                             # 文档
│   └── architecture.md               # 架构设计文档
│
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
      default: claude
      claude:
        api-key: ${ANTHROPIC_API_KEY:}
        model: claude-sonnet-4-6-20250514
      openai:
        api-key: ${OPENAI_API_KEY:}
        model: gpt-4o

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| `ANTHROPIC_API_KEY` | Claude API密钥 | - |
| `OPENAI_API_KEY` | OpenAI API密钥 | - |
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

```http
POST /api/chat
Authorization: Bearer {token}
Content-Type: application/json

{
  "message": "帮我创建一个采购申请，需要100个M8螺丝",
  "sessionId": "optional-session-id"
}
```

### 业务接口

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /api/users | 用户列表 |
| POST | /api/users | 创建用户 |
| GET | /api/suppliers | 供应商列表 |
| POST | /api/suppliers/recommend | AI推荐供应商 |
| GET | /api/purchase/requests | 采购申请列表 |
| POST | /api/purchase/requests | 创建采购申请 |

---

## 开发指南

### 运行测试

```bash
# 后端测试
cd ai-erp-server
mvn test

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

### v0.1.0 (MVP) ✅

- [x] 用户登录认证
- [x] AI对话交互
- [x] 采购申请基础功能
- [x] 供应商管理
- [x] 用户管理
- [x] 组织管理

### v0.2.0 (计划中)

- [ ] 流式对话响应
- [ ] Skills渐进式加载
- [ ] 工具调用可视化
- [ ] 发票OCR识别
- [ ] 合同智能审核

### v0.3.0 (规划中)

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

**⭐ 如果这个项目对你有帮助，请给一个Star支持一下！ ⭐**

Made with ❤️ by AI-ERP Team

</div>
