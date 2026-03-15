package com.aierp.ai.tools;

import com.aierp.entity.Warehouse;
import com.aierp.service.WarehouseService;
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
 * 仓库相关工具
 *
 * 提供仓库查询、新增等操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseTools {

    private final WarehouseService warehouseService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("创建仓库。需要仓库名称等基本信息。返回仓库ID和编码。")
    @ToolName("创建仓库")
    public String createWarehouse(
            @P("仓库名称，必填") String name,
            @P(value = "仓库类型，如：成品仓、原料仓、半成品仓", required = false) String type,
            @P(value = "地址", required = false) String address,
            @P(value = "负责人姓名", required = false) String manager,
            @P(value = "联系电话", required = false) String phone,
            @P(value = "备注", required = false) String remark) {

        try {
            log.info("创建仓库: name={}, type={}, manager={}", name, type, manager);

            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return errorResult("仓库名称不能为空");
            }

            // 生成仓库编码
            String code = generateWarehouseCode();

            Warehouse warehouse = new Warehouse();
            warehouse.setCode(code);
            warehouse.setName(name.trim());
            warehouse.setType(type != null ? type : "普通仓库");
            warehouse.setAddress(address);
            warehouse.setManager(manager);
            warehouse.setPhone(phone);
            warehouse.setRemark(remark);
            warehouse.setStatus("ACTIVE");

            Warehouse created = warehouseService.createWarehouse(warehouse);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", created.getId());
            result.put("code", created.getCode());
            result.put("name", created.getName());
            result.put("type", created.getType());
            result.put("message", "仓库创建成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建仓库失败", e);
            return errorResult("创建仓库失败: " + e.getMessage());
        }
    }

    @Tool("搜索仓库。支持关键词搜索和分页。返回仓库列表JSON。")
    @ToolName("搜索仓库")
    public String searchWarehouses(
            @P(value = "搜索关键词，可以是仓库名称或编码", required = false) String keyword,
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            log.info("搜索仓库: keyword={}, pageNum={}, pageSize={}", keyword, page, size);

            Page<Warehouse> pageResult = warehouseService.pageWarehouses(page, size, keyword, "ACTIVE");

            List<Map<String, Object>> warehouses = pageResult.getRecords().stream().map(wh -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", wh.getId());
                map.put("code", wh.getCode());
                map.put("name", wh.getName());
                map.put("type", wh.getType());
                map.put("address", wh.getAddress());
                map.put("manager", wh.getManager());
                map.put("phone", wh.getPhone());
                map.put("status", wh.getStatus());
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", page);
            result.put("pageSize", size);
            result.put("warehouses", warehouses);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("搜索仓库失败", e);
            return errorResult("搜索仓库失败: " + e.getMessage());
        }
    }

    @Tool("获取仓库详情。返回仓库完整信息JSON。")
    @ToolName("获取仓库详情")
    public String getWarehouseDetail(@P("仓库ID") Long warehouseId) {

        try {
            log.info("获取仓库详情: warehouseId={}", warehouseId);

            Warehouse warehouse = warehouseService.getById(warehouseId);
            if (warehouse == null) {
                return errorResult("仓库不存在: " + warehouseId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", warehouse.getId());
            result.put("code", warehouse.getCode());
            result.put("name", warehouse.getName());
            result.put("type", warehouse.getType());
            result.put("address", warehouse.getAddress());
            result.put("manager", warehouse.getManager());
            result.put("phone", warehouse.getPhone());
            result.put("status", warehouse.getStatus());
            result.put("remark", warehouse.getRemark());

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取仓库详情失败", e);
            return errorResult("获取仓库详情失败: " + e.getMessage());
        }
    }

    @Tool("获取所有可用仓库列表。返回所有状态正常的仓库。")
    @ToolName("获取所有仓库")
    public String listAllWarehouses() {

        try {
            log.info("获取所有可用仓库");

            Page<Warehouse> pageResult = warehouseService.pageWarehouses(1, 100, null, "ACTIVE");

            List<Map<String, Object>> warehouses = pageResult.getRecords().stream().map(wh -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", wh.getId());
                map.put("code", wh.getCode());
                map.put("name", wh.getName());
                map.put("type", wh.getType());
                map.put("manager", wh.getManager());
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", warehouses.size());
            result.put("warehouses", warehouses);
            result.put("message", warehouses.isEmpty() ? "暂无可用仓库" : "共找到" + warehouses.size() + "个仓库");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取仓库列表失败", e);
            return errorResult("获取仓库列表失败: " + e.getMessage());
        }
    }

    private String generateWarehouseCode() {
        return "WH" + System.currentTimeMillis();
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
