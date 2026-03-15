package com.aierp.ai.tools;

import com.aierp.context.UserContext;
import com.aierp.entity.Product;
import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.service.ProductService;
import com.aierp.service.PurchaseOrderService;
import com.aierp.service.SupplierService;
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
 * 采购订单工具
 *
 * 提供采购订单的创建、查询、审批、收货等操作
 * 用户信息通过 InvocationContext 获取
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseOrderTools {

    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从 InvocationContext 获取 UserContext
     */
    private UserContext getUserContext(InvocationContext context) {
        if (context == null) return null;
        return context.invocationParameters().get("userContext");
    }

    @Tool("创建采购订单。需要供应商ID和商品明细。productSku(商品编码)很重要，用于关联商品库存。返回订单号和ID。")
    public String createPurchaseOrder(
            @P("供应商ID，数字") Long supplierId,
            @P("采购明细列表，JSON数组格式。每项包含：productSku(商品编码，必填), productName(商品名称), quantity(数量), unit(单位), unitPrice(单价)") String itemsJson,
            InvocationContext context) {

        try {
            // 从 InvocationContext 获取用户信息
            UserContext userContext = getUserContext(context);
            Long userId = userContext != null ? userContext.getUserId() : null;
            String username = userContext != null ? userContext.getUsername() : null;

            log.info("创建采购订单: supplierId={}, items={}, userId={}, username={}", supplierId, itemsJson, userId, username);

            // 验证用户信息
            if (userId == null) {
                return errorResult("用户未登录，无法创建采购订单");
            }

            // 获取供应商名称
            var supplier = supplierService.getById(supplierId);
            if (supplier == null) {
                return errorResult("供应商不存在: " + supplierId);
            }

            Long buyerId = userId;
            String buyerName = username != null ? username : "用户" + userId;

            // 解析明细
            List<PurchaseOrderItem> items = parseItems(itemsJson);
            if (items.isEmpty()) {
                return errorResult("采购明细不能为空");
            }

            // 创建采购订单
            PurchaseOrder order = new PurchaseOrder();
            order.setSupplierId(supplierId);
            order.setSupplierName(supplier.getName());
            order.setBuyerId(buyerId);
            order.setBuyerName(buyerName);

            PurchaseOrder created = purchaseOrderService.createOrder(order, items);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", created.getId());
            result.put("orderNo", created.getOrderNo());
            result.put("status", created.getStatus());
            result.put("supplierName", supplier.getName());
            result.put("buyerName", buyerName);
            result.put("totalAmount", created.getTotalAmount());
            result.put("itemCount", items.size());
            result.put("message", "采购订单创建成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建采购订单失败", e);
            return errorResult("创建采购订单失败: " + e.getMessage());
        }
    }

    private List<PurchaseOrderItem> parseItems(String itemsJson) {
        List<PurchaseOrderItem> items = new ArrayList<>();
        if (itemsJson == null || itemsJson.isEmpty()) return items;

        try {
            if (itemsJson.trim().startsWith("[")) {
                List<Map> itemList = objectMapper.readValue(itemsJson, List.class);
                for (Map<String, Object> itemMap : itemList) {
                    PurchaseOrderItem item = new PurchaseOrderItem();
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
            log.warn("解析采购明细失败: {}", e.getMessage());
        }
        return items;
    }

    @Tool("查询采购订单列表。支持按状态筛选和分页。")
    public String queryPurchaseOrders(
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize,
            @P(value = "状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，RECEIVING-收货中，COMPLETED-已完成", required = false) String status) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            Page<PurchaseOrder> pageResult = purchaseOrderService.pageOrders(page, size, null, status);

            List<Map<String, Object>> orders = pageResult.getRecords().stream().map(order -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", order.getId());
                map.put("orderNo", order.getOrderNo());
                map.put("supplierName", order.getSupplierName());
                map.put("buyerName", order.getBuyerName());
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
            log.error("查询采购订单失败", e);
            return errorResult("查询采购订单失败: " + e.getMessage());
        }
    }

    @Tool("获取采购订单详情。返回订单信息和明细列表。")
    public String getPurchaseOrderDetail(@P("订单ID") Long orderId) {

        try {
            PurchaseOrder order = purchaseOrderService.getById(orderId);
            if (order == null) {
                return errorResult("采购订单不存在: " + orderId);
            }

            List<PurchaseOrderItem> items = purchaseOrderService.getOrderItems(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("supplierName", order.getSupplierName());
            result.put("buyerName", order.getBuyerName());
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
                map.put("receivedQty", item.getReceivedQty());
                map.put("status", item.getStatus());
                return map;
            }).toList();
            result.put("items", itemList);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取采购订单详情失败", e);
            return errorResult("获取采购订单详情失败: " + e.getMessage());
        }
    }

    @Tool("提交采购订单审批。将草稿状态的订单提交审批。")
    public String submitOrderForApproval(@P("订单ID") Long orderId) {

        try {
            PurchaseOrder order = purchaseOrderService.submitForApproval(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("message", "采购订单已提交审批");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("提交采购订单审批失败", e);
            return errorResult("提交采购订单审批失败: " + e.getMessage());
        }
    }

    @Tool("审批通过采购订单。")
    public String approvePurchaseOrder(@P("订单ID") Long orderId) {

        try {
            PurchaseOrder order = purchaseOrderService.approve(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("message", "采购订单已审批通过，可以进行收货操作");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("审批采购订单失败", e);
            return errorResult("审批采购订单失败: " + e.getMessage());
        }
    }

    @Tool("采购收货入库。需要仓库ID和收货明细。")
    public String receiveGoods(
            @P("订单ID") Long orderId,
            @P("仓库ID") Long warehouseId,
            @P("收货明细，JSON数组格式。每项包含：itemId(明细ID), quantity(收货数量)") String itemsJson,
            InvocationContext context) {

        try {
            // 从 InvocationContext 获取用户信息
            UserContext userContext = getUserContext(context);
            String operator = userContext != null ? userContext.getUsername() : "系统";

            // 解析收货明细
            List<Map<String, Object>> receiveItems = new ArrayList<>();
            if (itemsJson != null && itemsJson.trim().startsWith("[")) {
                List<Map> itemList = objectMapper.readValue(itemsJson, List.class);
                for (Map<String, Object> itemMap : itemList) {
                    Map<String, Object> receiveItem = new HashMap<>();
                    receiveItem.put("itemId", getLong(itemMap, "itemId"));
                    receiveItem.put("quantity", getBigDecimal(itemMap, "quantity"));
                    receiveItems.add(receiveItem);
                }
            }

            // 转换为收货请求格式
            List<com.aierp.dto.ReceiveRequest.ReceiveItem> receiveItemList = new ArrayList<>();
            for (Map<String, Object> item : receiveItems) {
                com.aierp.dto.ReceiveRequest.ReceiveItem receiveItem = new com.aierp.dto.ReceiveRequest.ReceiveItem();
                receiveItem.setItemId((Long) item.get("itemId"));
                receiveItem.setQuantity((BigDecimal) item.get("quantity"));
                receiveItemList.add(receiveItem);
            }

            PurchaseOrder order = purchaseOrderService.receiveGoods(orderId, warehouseId, receiveItemList, operator);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("message", "收货入库成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("采购收货入库失败", e);
            return errorResult("采购收货入库失败: " + e.getMessage());
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
            // 安全的 fallback：转义特殊字符
            String escaped = message != null ? message
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r") : "未知错误";
            return "{\"success\":false,\"error\":\"" + escaped + "\"}";
        }
    }
}
