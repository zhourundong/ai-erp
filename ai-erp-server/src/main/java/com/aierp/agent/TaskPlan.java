package com.aierp.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务计划
 *
 * 描述AI需要执行的任务步骤
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPlan {

    /**
     * 意图类型
     */
    private String intent;

    /**
     * 执行步骤
     */
    @Builder.Default
    private List<Step> steps = new ArrayList<>();

    /**
     * 是否需要人工确认
     */
    @Builder.Default
    private boolean requiresConfirmation = false;

    /**
     * 相关实体类型
     */
    private String entityType;

    /**
     * 提取的参数
     */
    private Object parameters;

    /**
     * 执行步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        /**
         * 步骤名称
         */
        private String name;

        /**
         * 工具名称
         */
        private String tool;

        /**
         * 步骤参数
         */
        private Object params;

        /**
         * 执行结果
         */
        private Object result;
    }
}
