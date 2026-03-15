package com.aierp.ai.tools;

import com.aierp.entity.Inventory;
import com.aierp.entity.InventoryTransaction;
import com.aierp.service.InventoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存相关工具
 *
 * 提供库存查询、流水查询等操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryTools {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("查询商品库存。可以按仓库和商品筛选。返回库存列表JSON。")
    @ToolName("查询库存")
    public String checkInventory(
            @P(value = "仓库ID。不填则查询所有仓库", required = false) Long warehouseId,
            @P(value = "商品ID。不填则查询所有商品", required = false) Long productId,
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            log.info("查询库存: warehouseId={}, productId={}, pageNum={}, pageSize={}",
                    warehouseId, productId, page, size);

            Page<Inventory> pageResult = inventoryService.pageInventory(page, size, warehouseId, productId);

            List<Map<String, Object>> inventories = pageResult.getRecords().stream().map(inv -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", inv.getId());
                map.put("warehouseId", inv.getWarehouseId());
                map.put("productId", inv.getProductId());
                map.put("productSku", inv.getProductSku());
                map.put("productName", inv.getProductName());
                map.put("quantity", inv.getQuantity());
                map.put("availableQty", inv.getAvailableQty());
                map.put("lockedQty", inv.getLockedQty());
                map.put("costPrice", inv.getCostPrice());
                map.put("lastInDate", inv.getLastInDate() != null ? inv.getLastInDate().toString() : null);
                map.put("lastOutDate", inv.getLastOutDate() != null ? inv.getLastOutDate().toString() : null);
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", page);
            result.put("pageSize", size);
            result.put("inventories", inventories);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("查询库存失败", e);
            return errorResult("查询库存失败: " + e.getMessage());
        }
    }

    @Tool("查询库存流水记录。支持按仓库、商品和交易类型筛选。返回流水列表JSON。")
    @ToolName("查询库存流水")
    public String queryTransactions(
            @P(value = "仓库ID。不填则查询所有仓库", required = false) Long warehouseId,
            @P(value = "商品ID。不填则查询所有商品", required = false) Long productId,
            @P(value = "交易类型：PURCHASE_IN-采购入库，SALES_OUT-销售出库，OTHER_IN-其他入库，OTHER_OUT-其他出库。不填则查询全部", required = false) String transactionType,
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            log.info("查询库存流水: warehouseId={}, productId={}, transactionType={}, pageNum={}, pageSize={}",
                    warehouseId, productId, transactionType, page, size);

            Page<InventoryTransaction> pageResult = inventoryService.pageTransactions(
                    page, size, warehouseId, productId, transactionType);

            List<Map<String, Object>> transactions = pageResult.getRecords().stream().map(txn -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", txn.getId());
                map.put("transactionNo", txn.getTransactionNo());
                map.put("transactionType", txn.getTransactionType());
                map.put("warehouseId", txn.getWarehouseId());
                map.put("warehouseName", txn.getWarehouseName());
                map.put("productId", txn.getProductId());
                map.put("productSku", txn.getProductSku());
                map.put("productName", txn.getProductName());
                map.put("quantity", txn.getQuantity());
                map.put("beforeQty", txn.getBeforeQty());
                map.put("afterQty", txn.getAfterQty());
                map.put("unitCost", txn.getUnitCost());
                map.put("totalCost", txn.getTotalCost());
                map.put("relatedOrderType", txn.getRelatedOrderType());
                map.put("relatedOrderNo", txn.getRelatedOrderNo());
                map.put("operator", txn.getOperator());
                map.put("createdAt", txn.getCreatedAt() != null ? txn.getCreatedAt().toString() : null);
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", page);
            result.put("pageSize", size);
            result.put("transactions", transactions);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("查询库存流水失败", e);
            return errorResult("查询库存流水失败: " + e.getMessage());
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
