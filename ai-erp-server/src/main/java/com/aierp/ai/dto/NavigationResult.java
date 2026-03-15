package com.aierp.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 导航结果DTO
 *
 * 用于AI工具返回导航指令，触发前端页面跳转
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationResult {

    /**
     * 动作类型
     * - navigate: 页面导航
     * - openCreateForm: 打开创建表单
     * - openModal: 打开详情弹窗
     */
    private String action;

    /**
     * 目标路径（如 /purchase-orders）
     */
    private String path;

    /**
     * 过滤条件（用于页面导航时自动过滤）
     */
    private Map<String, Object> filter;

    /**
     * 表单类型（用于 openCreateForm）
     */
    private String formType;

    /**
     * 记录类型（用于 openModal）
     */
    private String recordType;

    /**
     * 记录ID（用于 openModal）
     */
    private Long recordId;

    /**
     * 是否需要用户确认
     * - false: 自动执行（如简单导航）
     * - true: 显示确认按钮（如创建操作）
     */
    @Builder.Default
    private boolean requiresConfirmation = false;

    /**
     * 确认按钮文字
     */
    private String confirmText;

    /**
     * 描述信息（用于显示给用户）
     */
    private String description;

    /**
     * 创建简单的导航结果（自动执行）
     */
    public static NavigationResult navigate(String path) {
        return NavigationResult.builder()
                .action("navigate")
                .path(path)
                .requiresConfirmation(false)
                .build();
    }

    /**
     * 创建带过滤条件的导航结果（自动执行）
     */
    public static NavigationResult navigate(String path, Map<String, Object> filter) {
        return NavigationResult.builder()
                .action("navigate")
                .path(path)
                .filter(filter)
                .requiresConfirmation(false)
                .build();
    }

    /**
     * 创建打开表单的结果（需要确认）
     */
    public static NavigationResult openCreateForm(String formType, String path, String confirmText) {
        return NavigationResult.builder()
                .action("openCreateForm")
                .formType(formType)
                .path(path)
                .requiresConfirmation(true)
                .confirmText(confirmText)
                .build();
    }

    /**
     * 创建打开弹窗的结果（需要确认）
     */
    public static NavigationResult openModal(String recordType, Long recordId, String confirmText) {
        return NavigationResult.builder()
                .action("openModal")
                .recordType(recordType)
                .recordId(recordId)
                .requiresConfirmation(true)
                .confirmText(confirmText)
                .build();
    }
}
