# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AI-ERP 是一个 AI 原生的企业资源规划系统，AI 是核心基础设施而非附加功能。用户通过自然语言交互（CUI First 设计理念），AI 自主执行业务操作，人工进行监督。

## 构建与开发命令

### 后端 (Spring Boot)

```bash
cd ai-erp-server

# 启动服务 (端口 8080)
mvn spring-boot:run

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=PurchaseOrderServiceTest

# 运行单个测试方法
mvn test -Dtest=PurchaseOrderServiceTest#testCreateOrder_Success
```

### 前端 (React + Vite)

```bash
cd ai-erp-web

# 安装依赖
npm install

# 启动开发服务器 (端口 3000，代理到后端 8080)
npm run dev

# 生产构建
npm run build

# 代码检查
npm run lint

# 运行测试
npm run test
```

### 必需的环境变量

- `OPENAI_API_KEY` 或 `ANTHROPIC_API_KEY` - AI 模型 API 密钥（必需）
- `JWT_SECRET` - JWT 签名密钥（可选，有默认值）

## 技术架构

### 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 18 + TypeScript + Ant Design 5 + Zustand |
| 构建 | Vite |
| 后端 | Spring Boot 3.5.11 + MyBatis-Plus 3.5.5 |
| 数据库 | SQLite |
| AI 集成 | LangChain4j 1.12.2 |
| 认证 | Spring Security + JWT |

### 分层架构

```
Controller → AgentOrchestrator → ErpAssistant (AI Service) → Tools → Service → Mapper
                                    ↓
                              LangChain4j (AI Models)
```

### 后端关键包结构

| 包路径 | 用途 |
|--------|------|
| `com.aierp.ai` | AI 服务接口 (`ErpAssistant`) 和 DTO |
| `com.aierp.ai.tools` | AI 可调用的工具类 (PurchaseOrderTools, SalesOrderTools 等) |
| `com.aierp.agent` | Agent 编排 (`AgentOrchestrator`)，支持 SSE 流式响应 |
| `com.aierp.context` | 用户上下文，用于将用户信息传递给工具 |
| `com.aierp.config` | AI 模型配置 (`AiModelConfig`, `AiServiceConfig`) |

### AI 工具开发模式

工具类是 Spring Bean，方法使用 `@Tool` 注解。用户上下文通过 `InvocationParameters` 传递：

```java
@Component
public class PurchaseOrderTools {
    private final PurchaseOrderService purchaseOrderService;

    @Tool("创建采购订单")
    public PurchaseOrder createOrder(Long supplierId, List<OrderItem> items,
                                     InvocationContext context) {
        UserContext userContext = context.invocationParameters().get("userContext", UserContext.class);
        return purchaseOrderService.createOrder(supplierId, items, userContext.getUserId());
    }
}
```

### SSE 流式事件类型

ChatController 发送以下 SSE 事件类型：
- `thinking` - AI 思考过程（Base64 编码）
- `token` - 响应文本片段（Base64 编码）
- `tool` - 工具执行信息，包含名称、参数、结果
- `action` - 导航动作，用于前端路由跳转
- `complete` - 最终 ChatResponse
- `error` - 错误信息

### 前端状态管理

使用 Zustand 管理，位于 `src/stores/`：
- `authStore.ts` - 认证状态
- `chatStore.ts` - 聊天会话和消息
- `tabStore.ts` - Tab 导航状态

### 前端关键组件

- `MainLayout.tsx` - 主布局，包含固定侧边栏和 Tab 页签
- `FloatChat.tsx` - 悬浮 AI 聊天窗口，支持 SSE 流式响应
- `api.ts` - API 服务，支持 SSE

## 测试要求

每个新增的 Service 方法都需要编写单元测试。测试使用 Mockito 和 H2 数据库：

```java
@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {
    @Mock
    private PurchaseOrderMapper mapper;

    @InjectMocks
    private PurchaseOrderService service;

    @Test
    void testMethodName_Success() {
        // 测试正常流程
    }
}
```

## LangChain4j 参考

- 版本：1.12.2
- 文档：https://docs.langchain4j.dev/
- 关键类：`TokenStream`、`ToolExecution`、`InvocationParameters`

## 业务实体

核心实体位于 `com.aierp.entity`：
- `PurchaseOrder` / `PurchaseOrderItem` - 采购管理
- `SalesOrder` / `SalesOrderItem` - 销售管理
- `Inventory` / `InventoryTransaction` - 库存管理
- `Product`、`Supplier`、`Customer`、`Warehouse` - 基础数据
- `User`、`Organization` - 系统管理

## 默认登录账号

- 用户名：`admin`
- 密码：`admin123`
