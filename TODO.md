# AI-ERP 项目任务清单

> 最后更新: 2026-03-15

## 项目概览

AI-ERP 是一个 AI 原生的企业资源规划系统，采用 "CUI First" 设计理念，将 AI 能力深度融入业务流程。

---

## 已完成任务 ✅

### Phase 1: 基础架构 (v0.0.1)

#### 后端基础框架
- [x] Spring Boot 3.5.11 项目搭建
- [x] MyBatis-Plus ORM 集成
- [x] SQLite 数据库配置（MVP）
- [x] JWT 认证授权
- [x] 全局异常处理
- [x] 用户上下文管理 (UserContext)

#### 前端基础框架
- [x] React 18 + TypeScript 项目搭建
- [x] Ant Design 5.x UI 组件库集成
- [x] Zustand 状态管理
- [x] React Router 路由配置
- [x] Vite 构建工具配置

### Phase 2: 核心业务模块

#### 用户与组织
- [x] 用户管理 (UserController, UserService)
- [x] 组织架构管理 (OrganizationController, OrganizationService)
- [x] 用户登录认证 (AuthController, AuthService)

#### 基础数据
- [x] 商品管理 (ProductController, ProductService)
- [x] 供应商管理 (SupplierController, SupplierService)
- [x] 客户管理 (CustomerController, CustomerService)
- [x] 仓库管理 (WarehouseController, WarehouseService)

#### 采购管理
- [x] 采购申请 (PurchaseRequestController, PurchaseRequestService)
  - 创建、查询、修改、删除
  - 提交审批、审批通过、审批拒绝
  - 生成采购订单
- [x] 采购订单 (PurchaseOrderController, PurchaseOrderService)
  - 创建、查询、修改、删除
  - 提交审批、审批通过、审批拒绝、反审核
  - 收货入库

#### 销售管理
- [x] 销售订单 (SalesOrderController, SalesOrderService)
  - 创建、查询、修改、删除
  - 确认订单（锁定库存）
  - 发货出库
  - 取消订单

#### 库存管理
- [x] 库存查询 (InventoryController, InventoryService)
- [x] 库存流水记录
- [x] 入库/出库操作

#### 驾驶舱
- [x] 数据统计面板 (DashboardController, DashboardService)

### Phase 3: AI 能力

#### AI 集成框架
- [x] LangChain4j 1.12.2 集成
- [x] OpenAI 兼容模型配置
- [x] AI Services 架构 (ErpAssistant)
- [x] 流式响应 (SSE)
- [x] 会话记忆管理

#### AI 工具层
| 工具类 | 状态 | 功能 |
|--------|------|------|
| PurchaseOrderTools | ✅ | 创建/查询采购订单、审批、收货 |
| SalesOrderTools | ✅ | 创建/查询销售订单、确认、发货 |
| InventoryTools | ✅ | 查询库存数量、可用库存 |
| ProductTools | ✅ | 查询商品信息、按SKU查询 |
| SupplierTools | ✅ | 查询供应商、推荐供应商 |
| CustomerTools | ✅ | 查询客户信息、信用额度 |
| WarehouseTools | ✅ | 查询仓库信息 |
| NavigationTools | ✅ | 页面导航、打开表单、查看详情 |

#### AI 唤起 GUI 机制
- [x] 后端 NavigationTools 工具实现
- [x] SSE action 事件发送
- [x] 前端 action 事件监听
- [x] ActionCard 确认按钮组件
- [x] 自动导航 vs 需确认操作区分
- [x] 页面 pendingDetail 消费机制

### Phase 4: 前端页面

| 页面 | 状态 | 功能 |
|------|------|------|
| LoginPage | ✅ | 用户登录 |
| DashboardPage | ✅ | 首页统计 |
| ChatPage | ✅ | AI对话、流式响应、工具调用展示、思考过程 |
| ProductPage | ✅ | 商品CRUD |
| SupplierPage | ✅ | 供应商CRUD、推荐 |
| CustomerPage | ✅ | 客户CRUD |
| WarehousePage | ✅ | 仓库CRUD |
| PurchaseRequestPage | ✅ | 采购申请CRUD、审批流程 |
| PurchaseOrderPage | ✅ | 采购订单CRUD、审批、收货入库 |
| SalesOrderPage | ✅ | 销售订单CRUD、确认、发货 |
| InventoryPage | ✅ | 库存查询 |
| InventoryTransactionPage | ✅ | 库存流水 |
| UserManagePage | ✅ | 用户管理 |
| OrganizationPage | ✅ | 组织架构 |

### Phase 5: 测试覆盖

| 测试类型 | 文件数 | 状态 |
|----------|--------|------|
| Service Tests | 12 | ✅ |
| AI Tools Tests | 6 | ✅ |
| **总计** | **18** | ✅ |

---

## 进行中任务 🔄

> 当前无进行中任务

---

## 计划任务 📋

### v0.2.0 - 体验优化

#### 工具调用可视化
- [ ] 工具执行进度条动画
- [ ] 工具执行结果渲染优化（表格、图表）
- [ ] 工具调用链路展示

#### 多会话管理
- [ ] 会话列表持久化优化
- [ ] 会话搜索功能
- [ ] 会话导出功能

#### 响应优化
- [ ] 思考过程折叠动画
- [ ] 打字机效果优化
- [ ] 错误重试机制

### v0.3.0 - 智能增强

#### 智能识别
- [ ] 发票 OCR 识别
- [ ] 合同智能审核
- [ ] 商品图片识别

#### 智能推荐
- [ ] 智能采购建议
- [ ] 库存预警提醒
- [ ] 价格趋势分析

#### RAG 增强
- [ ] 企业知识库集成
- [ ] 历史订单检索
- [ ] 政策文档问答

### v0.4.0 - 扩展能力

#### 多租户支持
- [ ] 租户隔离架构
- [ ] 租户配置管理
- [ ] 数据权限控制

#### 移动端适配
- [ ] 响应式布局优化
- [ ] 移动端对话界面
- [ ] 语音输入支持

#### 系统集成
- [ ] 财务管理系统对接
- [ ] 生产管理系统对接
- [ ] WMS 仓储系统对接

---

## 技术债务 🔧

### 代码优化
- [ ] 前端大文件拆分 (DashboardPage > 500KB)
- [ ] API 响应类型完善
- [ ] 错误处理统一化

### 文档完善
- [x] README.md 更新
- [x] architecture.md 更新
- [ ] API 文档自动生成
- [ ] 部署文档编写

### 性能优化
- [ ] 数据库查询优化
- [ ] 前端首屏加载优化
- [ ] SSE 连接池管理

---

## 版本规划

| 版本 | 目标 | 状态 |
|------|------|------|
| v0.0.1 | MVP 基础功能 | ✅ 已完成 |
| v0.1.0 | AI 能力完善 | ✅ 已完成 |
| v0.2.0 | 体验优化 | 📋 计划中 |
| v0.3.0 | 智能增强 | 📋 规划中 |
| v0.4.0 | 扩展能力 | 📋 规划中 |

---

## 技术栈版本

### 后端
| 组件 | 版本 |
|------|------|
| Spring Boot | 3.5.11 |
| MyBatis-Plus | 3.5.5 |
| SQLite | 3.45.1 |
| LangChain4j | 1.12.2 |
| JWT (jjwt) | 0.12.5 |

### 前端
| 组件 | 版本 |
|------|------|
| React | 18.2.0 |
| TypeScript | 5.3.3 |
| Ant Design | 5.14.0 |
| Zustand | 4.5.0 |
| Vite | 5.1.0 |

---

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request
