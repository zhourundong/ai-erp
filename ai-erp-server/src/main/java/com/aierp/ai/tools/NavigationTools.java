package com.aierp.ai.tools;

import com.aierp.ai.dto.NavigationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AI导航工具
 *
 * 支持通过自然语言触发前端页面导航和操作
 * 实现CUI First设计理念的关键能力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NavigationTools {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 页面路径映射
     */
    private static final Map<String, String> PAGE_PATHS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("purchase-orders", "/purchase-orders");
        map.put("sales-orders", "/sales-orders");
        map.put("inventory", "/inventory");
        map.put("products", "/products");
        map.put("suppliers", "/suppliers");
        map.put("customers", "/customers");
        map.put("warehouses", "/warehouses");
        map.put("purchase-requests", "/purchase-requests");
        map.put("dashboard", "/");
        map.put("home", "/");
        PAGE_PATHS = Map.copyOf(map);
    }

    /**
     * 表单路径映射
     */
    private static final Map<String, String> FORM_PATHS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("purchase-order", "/purchase-orders?create=true");
        map.put("sales-order", "/sales-orders?create=true");
        map.put("product", "/products?create=true");
        map.put("supplier", "/suppliers?create=true");
        map.put("customer", "/customers?create=true");
        map.put("warehouse", "/warehouses?create=true");
        map.put("purchase-request", "/purchase-requests?create=true");
        FORM_PATHS = Map.copyOf(map);
    }

    /**
     * 记录类型路径映射
     */
    private static final Map<String, String> RECORD_PATHS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("purchase-order", "/purchase-orders");
        map.put("sales-order", "/sales-orders");
        map.put("product", "/products");
        map.put("supplier", "/suppliers");
        map.put("customer", "/customers");
        map.put("warehouse", "/warehouses");
        map.put("purchase-request", "/purchase-requests");
        RECORD_PATHS = Map.copyOf(map);
    }

    @Tool("导航到指定页面。当用户要求打开某个页面、查看列表、浏览数据时使用。例如：'打开采购订单列表'、'查看库存'、'去商品管理'")
    public String navigate(
        @P("目标页面: purchase-orders(采购订单), sales-orders(销售订单), inventory(库存), products(商品), suppliers(供应商), customers(客户), warehouses(仓库), purchase-requests(采购申请), dashboard(仪表盘)") String page,
        @P(value = "可选的过滤条件JSON，如 {\"status\":\"PENDING\"} 或 {\"keyword\":\"关键词\"}", required = false) String filter
    ) {
        log.info("导航工具调用: page={}, filter={}", page, filter);

        String path = PAGE_PATHS.getOrDefault(page, "/" + page);
        Map<String, Object> filterMap = parseFilter(filter);

        NavigationResult result = NavigationResult.builder()
            .action("navigate")
            .path(path)
            .filter(filterMap)
            .requiresConfirmation(false)  // 简单导航自动执行
            .description("打开" + getPageDescription(page))
            .build();

        return toJson(result);
    }

    @Tool("打开创建表单。当用户要求创建新记录、新增数据时使用。例如：'创建采购订单'、'新增商品'、'添加供应商'")
    public String openCreateForm(
        @P("表单类型: purchase-order(采购订单), sales-order(销售订单), product(商品), supplier(供应商), customer(客户), warehouse(仓库), purchase-request(采购申请)") String formType
    ) {
        log.info("打开创建表单: formType={}", formType);

        String path = FORM_PATHS.get(formType);
        if (path == null) {
            path = "/" + formType + "?create=true";
        }

        String description = "创建" + getFormDescription(formType);
        String confirmText = "立即创建";

        NavigationResult result = NavigationResult.builder()
            .action("openCreateForm")
            .path(path)
            .formType(formType)
            .requiresConfirmation(true)  // 创建操作需要确认
            .confirmText(confirmText)
            .description(description)
            .build();

        return toJson(result);
    }

    @Tool("打开详情页面。当用户要求查看某条记录的详细信息时使用。例如：'查看订单详情'、'显示商品信息'")
    public String openDetail(
        @P("记录类型: purchase-order(采购订单), sales-order(销售订单), product(商品), supplier(供应商), customer(客户)") String recordType,
        @P("记录ID，必须是有效的数字ID") Long recordId
    ) {
        log.info("打开详情页面: recordType={}, recordId={}", recordType, recordId);

        if (recordId == null || recordId <= 0) {
            return errorResult("无效的记录ID");
        }

        String basePath = RECORD_PATHS.get(recordType);
        if (basePath == null) {
            basePath = "/" + recordType;
        }

        String path = basePath + "/" + recordId;

        NavigationResult result = NavigationResult.builder()
            .action("navigate")
            .path(path)
            .recordType(recordType)
            .recordId(recordId)
            .requiresConfirmation(false)  // 查看详情自动执行
            .description("查看" + getRecordDescription(recordType) + "详情")
            .build();

        return toJson(result);
    }

    /**
     * 解析过滤条件
     */
    private Map<String, Object> parseFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(filter, Map.class);
        } catch (JsonProcessingException e) {
            // 如果不是JSON格式，作为关键词处理
            Map<String, Object> result = new HashMap<>();
            result.put("keyword", filter);
            return result;
        }
    }

    /**
     * 转换为JSON字符串
     */
    private String toJson(NavigationResult result) {
        try {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("success", true);
            wrapper.put("navigation", result);
            return objectMapper.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            log.error("序列化导航结果失败", e);
            return errorResult("序列化失败");
        }
    }

    /**
     * 错误结果
     */
    private String errorResult(String message) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", message);
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"success\":false,\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
        }
    }

    /**
     * 获取页面描述
     */
    private String getPageDescription(String page) {
        return switch (page) {
            case "purchase-orders" -> "采购订单列表";
            case "sales-orders" -> "销售订单列表";
            case "inventory" -> "库存查询";
            case "products" -> "商品管理";
            case "suppliers" -> "供应商管理";
            case "customers" -> "客户管理";
            case "warehouses" -> "仓库管理";
            case "purchase-requests" -> "采购申请列表";
            case "dashboard", "home" -> "仪表盘";
            default -> page;
        };
    }

    /**
     * 获取表单描述
     */
    private String getFormDescription(String formType) {
        return switch (formType) {
            case "purchase-order" -> "采购订单";
            case "sales-order" -> "销售订单";
            case "product" -> "商品";
            case "supplier" -> "供应商";
            case "customer" -> "客户";
            case "warehouse" -> "仓库";
            case "purchase-request" -> "采购申请";
            default -> formType;
        };
    }

    /**
     * 获取记录描述
     */
    private String getRecordDescription(String recordType) {
        return switch (recordType) {
            case "purchase-order" -> "采购订单";
            case "sales-order" -> "销售订单";
            case "product" -> "商品";
            case "supplier" -> "供应商";
            case "customer" -> "客户";
            case "warehouse" -> "仓库";
            case "purchase-request" -> "采购申请";
            default -> recordType;
        };
    }
}
