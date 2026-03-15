package com.aierp.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务规划器
 *
 * 根据意图生成任务执行计划
 */
@Slf4j
@Component
public class TaskPlanner {

    /**
     * 生成任务计划
     */
    public TaskPlan plan(String intent, String message) {
        TaskPlan.TaskPlanBuilder builder = TaskPlan.builder()
                .intent(intent);

        // 根据意图生成执行步骤
        switch (intent) {
            case "CREATE_PURCHASE_REQUEST":
                builder.entityType("PurchaseRequest")
                        .requiresConfirmation(true)
                        .steps(List.of(
                                TaskPlan.Step.builder().name("解析需求").tool("parseRequirement").build(),
                                TaskPlan.Step.builder().name("匹配物料").tool("matchMaterial").build(),
                                TaskPlan.Step.builder().name("生成申请").tool("createPurchaseRequest").build()
                        ));
                break;

            case "QUERY_SUPPLIER":
                builder.entityType("Supplier")
                        .steps(List.of(
                                TaskPlan.Step.builder().name("搜索供应商").tool("searchSupplier").build()
                        ));
                break;

            case "RECOMMEND_SUPPLIER":
                builder.entityType("Supplier")
                        .requiresConfirmation(false)
                        .steps(List.of(
                                TaskPlan.Step.builder().name("分析需求").tool("analyzeRequirement").build(),
                                TaskPlan.Step.builder().name("筛选供应商").tool("filterSuppliers").build(),
                                TaskPlan.Step.builder().name("计算推荐分数").tool("calculateScore").build()
                        ));
                break;

            case "ANALYZE_DATA":
                builder.requiresConfirmation(false)
                        .steps(List.of(
                                TaskPlan.Step.builder().name("收集数据").tool("collectData").build(),
                                TaskPlan.Step.builder().name("分析统计").tool("analyzeStatistics").build(),
                                TaskPlan.Step.builder().name("生成洞察").tool("generateInsights").build()
                        ));
                break;

            default:
                builder.requiresConfirmation(false)
                        .steps(List.of(
                                TaskPlan.Step.builder().name("通用处理").tool("generalProcess").build()
                        ));
        }

        return builder.build();
    }
}
