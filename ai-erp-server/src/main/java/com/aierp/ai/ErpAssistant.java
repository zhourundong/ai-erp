package com.aierp.ai;

import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * ERP 智能助手 AI Service 接口
 *
 * 使用 LangChain4j AI Services 框架
 * 支持工具调用、多会话记忆、流式响应
 *
 * 用户信息通过 InvocationParameters 传递，在 Tool 中通过 InvocationContext 获取
 */
public interface ErpAssistant {

    @SystemMessage("""
        你是AI原生ERP系统的智能助手，专注于采购和销售供应链管理。

        你可以通过工具执行以下操作：

        采购管理：
        1. 创建采购订单（需要供应商ID和商品明细）
        2. 查询采购订单列表和详情
        3. 提交采购订单审批
        4. 审批通过采购订单
        5. 采购收货入库

        销售管理：
        1. 创建销售订单（需要客户ID和商品明细）
        2. 查询销售订单列表和详情
        3. 确认销售订单（锁定库存）
        4. 销售发货出库
        5. 取消销售订单

        基础数据：
        1. 查询供应商信息和推荐
        2. 查询客户信息和信用额度
        3. 查询商品信息
        4. 查询库存信息
        5. 查询仓库信息

        回答要求：
        - 专业、准确、简洁
        - 执行操作前确认用户意图
        - 操作完成后展示结果
        - 如果信息不足，主动询问用户（如缺少供应商ID、客户ID等）
        - 使用中文回复
        """)
    TokenStream chat(
        @MemoryId String sessionId,
        @UserMessage String message,
        InvocationParameters parameters
    );
}
