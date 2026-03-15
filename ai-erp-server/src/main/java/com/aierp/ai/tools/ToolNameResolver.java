package com.aierp.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具名称解析器
 * 根据 @ToolName 注解获取工具方法的中文名称
 */
@Slf4j
@Component
public class ToolNameResolver {

    // 缓存工具名称映射
    private final Map<String, String> toolNameCache = new HashMap<>();

    // 工具类列表
    private final Object[] toolBeans;

    public ToolNameResolver(
            PurchaseOrderTools purchaseOrderTools,
            SalesOrderTools salesOrderTools,
            InventoryTools inventoryTools,
            ProductTools productTools,
            SupplierTools supplierTools,
            CustomerTools customerTools,
            WarehouseTools warehouseTools,
            NavigationTools navigationTools
    ) {
        this.toolBeans = new Object[]{
                purchaseOrderTools,
                salesOrderTools,
                inventoryTools,
                productTools,
                supplierTools,
                customerTools,
                warehouseTools,
                navigationTools
        };

        // 初始化时扫描所有工具方法
        scanToolMethods();
    }

    /**
     * 扫描所有工具方法，构建名称映射
     */
    private void scanToolMethods() {
        for (Object toolBean : toolBeans) {
            if (toolBean == null) continue;

            Class<?> clazz = toolBean.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                ToolName toolNameAnnotation = method.getAnnotation(ToolName.class);
                if (toolNameAnnotation != null) {
                    String key = method.getName();
                    String chineseName = toolNameAnnotation.value();
                    toolNameCache.put(key, chineseName);
                    log.debug("注册工具名称: {} -> {}", key, chineseName);
                }
            }
        }
        log.info("已注册 {} 个工具名称", toolNameCache.size());
    }

    /**
     * 根据方法名获取中文名称
     *
     * @param methodName 工具方法名
     * @return 中文名称，如果没有则返回方法名本身
     */
    public String getChineseName(String methodName) {
        return toolNameCache.getOrDefault(methodName, methodName);
    }

    /**
     * 根据工具执行对象获取中文名称
     * 支持多种格式：methodName 或 className.methodName
     *
     * @param toolName 工具名称
     * @return 中文名称
     */
    public String resolveChineseName(String toolName) {
        if (toolName == null || toolName.isEmpty()) {
            return toolName;
        }

        // 尝试直接匹配
        String chineseName = toolNameCache.get(toolName);
        if (chineseName != null) {
            return chineseName;
        }

        // 如果包含点号，提取方法名部分
        if (toolName.contains(".")) {
            String methodName = toolName.substring(toolName.lastIndexOf(".") + 1);
            chineseName = toolNameCache.get(methodName);
            if (chineseName != null) {
                return chineseName;
            }
        }

        // 未找到映射，返回原名
        return toolName;
    }
}
