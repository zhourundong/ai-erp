package com.aierp.agent;

import com.aierp.ai.ModelAdapter;
import com.aierp.ai.dto.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图识别器
 *
 * 分析用户输入，识别其意图类型
 */
@Slf4j
@Component
public class IntentClassifier {

    /**
     * 意图类型
     */
    public enum Intent {
        // 采购申请相关
        CREATE_PURCHASE_REQUEST,    // 创建采购申请
        QUERY_PURCHASE_REQUEST,     // 查询采购申请
        APPROVE_PURCHASE_REQUEST,   // 审批采购申请

        // 采购订单相关
        CREATE_PURCHASE_ORDER,      // 创建采购订单
        QUERY_PURCHASE_ORDER,       // 查询采购订单
        CANCEL_PURCHASE_ORDER,      // 取消采购订单

        // 供应商相关
        QUERY_SUPPLIER,             // 查询供应商
        RECOMMEND_SUPPLIER,         // 推荐供应商
        EVALUATE_SUPPLIER,          // 评估供应商

        // 物料相关
        QUERY_MATERIAL,             // 查询物料
        CHECK_INVENTORY,            // 查询库存

        // 数据分析
        ANALYZE_DATA,               // 数据分析
        GENERATE_REPORT,            // 生成报表

        // 通用
        GENERAL_QUESTION,           // 通用问题
        UNKNOWN                     // 未知意图
    }

    /**
     * 识别用户意图
     */
    public String classify(String message, ModelAdapter model) {
        String lowerMessage = message.toLowerCase();

        // 基于关键词的简单意图识别
        // TODO: 后续可以使用AI模型进行更准确的意图识别

        if (containsAny(lowerMessage, "创建", "新建", "提交", "申请采购", "需要采购")) {
            if (containsAny(lowerMessage, "采购申请", "请购")) {
                return Intent.CREATE_PURCHASE_REQUEST.name();
            }
            if (containsAny(lowerMessage, "采购订单", "订单")) {
                return Intent.CREATE_PURCHASE_ORDER.name();
            }
            return Intent.CREATE_PURCHASE_REQUEST.name();
        }

        if (containsAny(lowerMessage, "查询", "查看", "搜索", "找", "列出")) {
            if (containsAny(lowerMessage, "供应商")) {
                return Intent.QUERY_SUPPLIER.name();
            }
            if (containsAny(lowerMessage, "采购申请", "请购")) {
                return Intent.QUERY_PURCHASE_REQUEST.name();
            }
            if (containsAny(lowerMessage, "采购订单", "订单")) {
                return Intent.QUERY_PURCHASE_ORDER.name();
            }
            if (containsAny(lowerMessage, "物料", "库存")) {
                return Intent.QUERY_MATERIAL.name();
            }
        }

        if (containsAny(lowerMessage, "推荐", "建议", "选择供应商")) {
            return Intent.RECOMMEND_SUPPLIER.name();
        }

        if (containsAny(lowerMessage, "评估", "评价", "分析供应商")) {
            return Intent.EVALUATE_SUPPLIER.name();
        }

        if (containsAny(lowerMessage, "审批", "批准", "审核")) {
            return Intent.APPROVE_PURCHASE_REQUEST.name();
        }

        if (containsAny(lowerMessage, "分析", "统计", "报表", "报告")) {
            return Intent.ANALYZE_DATA.name();
        }

        if (containsAny(lowerMessage, "取消", "删除")) {
            return Intent.CANCEL_PURCHASE_ORDER.name();
        }

        return Intent.GENERAL_QUESTION.name();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
