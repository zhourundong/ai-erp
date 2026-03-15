package com.aierp.ai.tools;

import com.aierp.entity.Supplier;
import com.aierp.service.SupplierService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商相关工具
 *
 * 提供供应商查询、推荐、新增等操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierTools {

    private final SupplierService supplierService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("创建供应商。需要供应商基本信息。返回供应商ID和编码。")
    @ToolName("创建供应商")
    public String createSupplier(
            @P("供应商名称，必填") String name,
            @P(value = "供应商简称", required = false) String shortName,
            @P(value = "供应商类别，如：办公用品、电子设备、原材料", required = false) String category,
            @P(value = "联系人姓名", required = false) String contactPerson,
            @P(value = "联系电话", required = false) String contactPhone,
            @P(value = "联系邮箱", required = false) String contactEmail,
            @P(value = "地址", required = false) String address,
            @P(value = "开户行", required = false) String bankName,
            @P(value = "银行账户", required = false) String bankAccount,
            @P(value = "税号", required = false) String taxNumber,
            @P(value = "备注", required = false) String remark) {

        try {
            log.info("创建供应商: name={}, category={}, contactPerson={}", name, category, contactPerson);

            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return errorResult("供应商名称不能为空");
            }

            // 生成供应商编码
            String code = generateSupplierCode();

            Supplier supplier = new Supplier();
            supplier.setCode(code);
            supplier.setName(name.trim());
            supplier.setShortName(shortName);
            supplier.setCategory(category);
            supplier.setContactPerson(contactPerson);
            supplier.setContactPhone(contactPhone);
            supplier.setContactEmail(contactEmail);
            supplier.setAddress(address);
            supplier.setBankName(bankName);
            supplier.setBankAccount(bankAccount);
            supplier.setTaxNumber(taxNumber);
            supplier.setRemark(remark);
            supplier.setLevel("普通");  // 默认等级
            supplier.setRating(3);  // 默认评级
            supplier.setScore(new BigDecimal("70"));  // 默认评分
            supplier.setTransactionCount(0);
            supplier.setTotalTransactionAmount(BigDecimal.ZERO);
            supplier.setStatus("ACTIVE");

            Supplier created = supplierService.createSupplier(supplier);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", created.getId());
            result.put("code", created.getCode());
            result.put("name", created.getName());
            result.put("message", "供应商创建成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建供应商失败", e);
            return errorResult("创建供应商失败: " + e.getMessage());
        }
    }

    @Tool("搜索供应商。支持关键词搜索和分页。返回供应商列表JSON。")
    @ToolName("搜索供应商")
    public String searchSuppliers(
            @P(value = "搜索关键词，可以是供应商名称、编码或联系人", required = false) String keyword,
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            log.info("搜索供应商: keyword={}, pageNum={}, pageSize={}", keyword, page, size);

            Page<Supplier> pageResult = supplierService.pageSuppliers(page, size, keyword, "ACTIVE");

            List<Map<String, Object>> suppliers = pageResult.getRecords().stream().map(sup -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", sup.getId());
                map.put("code", sup.getCode());
                map.put("name", sup.getName());
                map.put("shortName", sup.getShortName());
                map.put("category", sup.getCategory());
                map.put("contactPerson", sup.getContactPerson());
                map.put("contactPhone", sup.getContactPhone());
                map.put("score", sup.getScore());
                map.put("rating", sup.getRating());
                map.put("transactionCount", sup.getTransactionCount());
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", page);
            result.put("pageSize", size);
            result.put("suppliers", suppliers);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("搜索供应商失败", e);
            return errorResult("搜索供应商失败: " + e.getMessage());
        }
    }

    @Tool("推荐优质供应商。根据商品类别和最低评分筛选推荐供应商。返回推荐列表JSON。")
    @ToolName("推荐供应商")
    public String recommendSuppliers(
            @P(value = "商品类别，如：办公用品、电子设备、原材料。不填则推荐所有类别", required = false) String category,
            @P(value = "最低评分，数字，默认70分", required = false) Double minScore) {

        try {
            BigDecimal minScoreValue = minScore != null ? BigDecimal.valueOf(minScore) : new BigDecimal("70");

            log.info("推荐供应商: category={}, minScore={}", category, minScoreValue);

            List<Supplier> suppliers = supplierService.recommendSuppliers(category, minScoreValue);

            List<Map<String, Object>> supplierList = suppliers.stream().map(sup -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", sup.getId());
                map.put("code", sup.getCode());
                map.put("name", sup.getName());
                map.put("shortName", sup.getShortName());
                map.put("category", sup.getCategory());
                map.put("contactPerson", sup.getContactPerson());
                map.put("contactPhone", sup.getContactPhone());
                map.put("score", sup.getScore());
                map.put("rating", sup.getRating());
                map.put("transactionCount", sup.getTransactionCount());
                map.put("totalTransactionAmount", sup.getTotalTransactionAmount());
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", supplierList.size());
            result.put("category", category);
            result.put("minScore", minScoreValue);
            result.put("suppliers", supplierList);
            result.put("message", supplierList.isEmpty() ? "未找到符合条件的供应商" : "已找到" + supplierList.size() + "家优质供应商");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("推荐供应商失败", e);
            return errorResult("推荐供应商失败: " + e.getMessage());
        }
    }

    @Tool("获取供应商详情。返回供应商完整信息JSON。")
    @ToolName("获取供应商详情")
    public String getSupplierDetail(@P("供应商ID") Long supplierId) {

        try {
            log.info("获取供应商详情: supplierId={}", supplierId);

            Supplier supplier = supplierService.getSupplierById(supplierId);
            if (supplier == null) {
                return errorResult("供应商不存在: " + supplierId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", supplier.getId());
            result.put("code", supplier.getCode());
            result.put("name", supplier.getName());
            result.put("shortName", supplier.getShortName());
            result.put("category", supplier.getCategory());
            result.put("level", supplier.getLevel());
            result.put("contactPerson", supplier.getContactPerson());
            result.put("contactPhone", supplier.getContactPhone());
            result.put("contactEmail", supplier.getContactEmail());
            result.put("address", supplier.getAddress());
            result.put("bankName", supplier.getBankName());
            result.put("bankAccount", supplier.getBankAccount());
            result.put("taxNumber", supplier.getTaxNumber());
            result.put("rating", supplier.getRating());
            result.put("score", supplier.getScore());
            result.put("transactionCount", supplier.getTransactionCount());
            result.put("totalTransactionAmount", supplier.getTotalTransactionAmount());
            result.put("status", supplier.getStatus());
            result.put("remark", supplier.getRemark());

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取供应商详情失败", e);
            return errorResult("获取供应商详情失败: " + e.getMessage());
        }
    }

    private String generateSupplierCode() {
        return "SUP" + System.currentTimeMillis();
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
