package com.aierp.ai.tools;

import com.aierp.context.UserContext;
import com.aierp.dto.ShipRequest;
import com.aierp.entity.Product;
import com.aierp.entity.SalesOrder;
import com.aierp.entity.SalesOrderItem;
import com.aierp.service.CustomerService;
import com.aierp.service.ProductService;
import com.aierp.service.SalesOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售订单工具
 *
 * 提供销售订单的创建、查询、确认、发货等操作
 * 用户信息通过 InvocationContext 获取
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesOrderTools {

    private final SalesOrderService salesOrderService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从 InvocationContext 获取 UserContext
     */
    private UserContext getUserContext(InvocationContext context) {
        if (context == null) return null;
        return context.invocationParameters().get("userContext");
    }

    @Tool("创建销售订单。需要客户ID和商品明细。productSku(商品编码)很重要，用于关联商品库存。返回订单号和ID。")
    public String createSalesOrder(
            @P("客户ID，数字") Long customerId,
            @P("销售明细列表，JSON数组格式。每项包含：productSku(商品编码，必填), productName(商品名称), quantity(数量), unit(单位), unitPrice(单价)") String itemsJson,
            InvocationContext context) {

        try {
            // 从 InvocationContext 获取用户信息
            UserContext userContext = getUserContext(context);
            Long userId = userContext != null ? userContext.getUserId() : null;
            String username = userContext != null ? userContext.getUsername() : null;

            log.info("创建销售订单: customerId={}, items={}, userId={}, username={}", customerId, itemsJson, userId, username);

            // 验证用户信息
            if (userId == null) {
                return errorResult("用户未登录，无法创建销售订单");
            }

            // 获取客户名称
            var customer = customerService.getById(customerId);
            if (customer == null) {
                return errorResult("客户不存在: " + customerId);
            }

            Long salesId = userId;
            String salesName = username != null ? username : "用户" + userId;

            // 解析明细
            List<SalesOrderItem> items = parseItems(itemsJson);
            if (items.isEmpty()) {
                return errorResult("销售明细不能为空");
            }

            // 创建销售订单
            SalesOrder order = new SalesOrder();
            order.setCustomerId(customerId);
            order.setCustomerName(customer.getName());
            order.setSalesId(salesId);
            order.setSalesName(salesName);

            SalesOrder created = salesOrderService.createOrder(order, items);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", created.getId());
            result.put("orderNo", created.getOrderNo());
            result.put("status", created.getStatus());
            result.put("customerName", customer.getName());
            result.put("salesName", salesName);
            result.put("totalAmount", created.getTotalAmount());
            result.put("itemCount", items.size());
            result.put("message", "销售订单创建成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建销售订单失败", e);
            return errorResult("创建销售订单失败: " + e.getMessage());
        }
    }

    private List<SalesOrderItem> parseItems(String itemsJson) {
        List<SalesOrderItem> items = new ArrayList<>();
        if (itemsJson == null || itemsJson.isEmpty()) return items;

        try {
            if (itemsJson.trim().startsWith("[")) {
                List<Map> itemList = objectMapper.readValue(itemsJson, List.class);
                for (Map<String, Object> itemMap : itemList) {
                    SalesOrderItem item = new SalesOrderItem();
                    String productSku = getString(itemMap, "productSku");
                    String productName = getString(itemMap, "productName");

                    // 通过 SKU 查找商品，获取 productId
                    if (productSku != null && !productSku.isEmpty()) {
                        Product product = productService.getBySku(productSku);
                        if (product != null) {
                            item.setProductId(product.getId());
                            // 如果没有提供商品名称，使用数据库中的名称
                            if (productName == null || productName.isEmpty()) {
                                productName = product.getName();
                            }
                        }
                    }

                    item.setProductName(productName);
                    item.setProductSku(productSku);
                    item.setQuantity(getBigDecimal(itemMap, "quantity"));
                    item.setUnit(getString(itemMap, "unit", "个"));
                    item.setUnitPrice(getBigDecimal(itemMap, "unitPrice"));
                    items.add(item);
                }
            }
        } catch (Exception e) {
            log.warn("解析销售明细失败: {}", e.getMessage());
        }
        return items;
    }

    @Tool("查询销售订单列表。支持按状态筛选和分页。")
    public String querySalesOrders(
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize,
            @P(value = "状态：DRAFT-草稿，CONFIRMED-已确认，SHIPPING-发货中，COMPLETED-已完成，CANCELLED-已取消", required = false) String status) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            Page<SalesOrder> pageResult = salesOrderService.pageOrders(page, size, null, status);

            List<Map<String, Object>> orders = pageResult.getRecords().stream().map(order -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", order.getId());
                map.put("orderNo", order.getOrderNo());
                map.put("customerName", order.getCustomerName());
                map.put("salesName", order.getSalesName());
                map.put("totalAmount", order.getTotalAmount());
                map.put("status", order.getStatus());
                map.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null);
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("orders", orders);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("查询销售订单失败", e);
            return errorResult("查询销售订单失败: " + e.getMessage());
        }
    }

    @Tool("获取销售订单详情。返回订单信息和明细列表。")
    public String getSalesOrderDetail(@P("订单ID") Long orderId) {

        try {
            SalesOrder order = salesOrderService.getById(orderId);
            if (order == null) {
                return errorResult("销售订单不存在: " + orderId);
            }

            List<SalesOrderItem> items = salesOrderService.getOrderItems(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("customerName", order.getCustomerName());
            result.put("salesName", order.getSalesName());
            result.put("totalAmount", order.getTotalAmount());
            result.put("status", order.getStatus());
            result.put("paymentStatus", order.getPaymentStatus());

            List<Map<String, Object>> itemList = items.stream().map(item -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("productName", item.getProductName());
                map.put("productSku", item.getProductSku());
                map.put("quantity", item.getQuantity());
                map.put("unit", item.getUnit());
                map.put("unitPrice", item.getUnitPrice());
                map.put("amount", item.getAmount());
                map.put("shippedQty", item.getShippedQty());
                map.put("status", item.getStatus());
                return map;
            }).toList();
            result.put("items", itemList);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取销售订单详情失败", e);
            return errorResult("获取销售订单详情失败: " + e.getMessage());
        }
    }

    @Tool("确认销售订单。需要指定出库仓库。确认后会锁定库存。")
    public String confirmSalesOrder(
            @P("订单ID") Long orderId,
            @P("仓库ID，发货出库的仓库") Long warehouseId) {

        try {
            SalesOrder order = salesOrderService.confirm(orderId, warehouseId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("message", "销售订单已确认，库存已锁定，可以进行发货操作");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("确认销售订单失败", e);
            return errorResult("确认销售订单失败: " + e.getMessage());
        }
    }

    @Tool("销售发货出库。需要仓库ID和发货明细。")
    public String shipGoods(
            @P("订单ID") Long orderId,
            @P("仓库ID") Long warehouseId,
            @P("发货明细，JSON数组格式。每项包含：itemId(明细ID), quantity(发货数量)") String itemsJson,
            InvocationContext context) {

        try {
            // 从 InvocationContext 获取用户信息
            UserContext userContext = getUserContext(context);
            String operator = userContext != null ? userContext.getUsername() : "系统";

            // 解析发货明细
            List<ShipRequest.ShipItem> shipItems = new ArrayList<>();
            if (itemsJson != null && itemsJson.trim().startsWith("[")) {
                List<Map> itemList = objectMapper.readValue(itemsJson, List.class);
                for (Map<String, Object> itemMap : itemList) {
                    ShipRequest.ShipItem shipItem = new ShipRequest.ShipItem();
                    shipItem.setItemId(getLong(itemMap, "itemId"));
                    shipItem.setQuantity(getBigDecimal(itemMap, "quantity"));
                    shipItems.add(shipItem);
                }
            }

            SalesOrder order = salesOrderService.shipGoods(orderId, warehouseId, shipItems, operator);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("message", "发货出库成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("销售发货出库失败", e);
            return errorResult("销售发货出库失败: " + e.getMessage());
        }
    }

    @Tool("取消销售订单。只有草稿状态的订单可以取消。")
    public String cancelSalesOrder(@P("订单ID") Long orderId) {

        try {
            SalesOrder order = salesOrderService.cancel(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("message", "销售订单已取消");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("取消销售订单失败", e);
            return errorResult("取消销售订单失败: " + e.getMessage());
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        String value = getString(map, key);
        return value != null ? value : defaultValue;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String errorResult(String message) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", message);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            String escaped = message != null ? message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") : "未知错误";
            return "{\"success\":false,\"error\":\"" + escaped + "\"}";
        }
    }
}
