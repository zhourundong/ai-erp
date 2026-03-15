package com.aierp.ai.tools;

import com.aierp.entity.Customer;
import com.aierp.service.CustomerService;
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
 * 客户相关工具
 *
 * 提供客户查询、信用额度查询、新增等操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerTools {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("创建客户。需要客户基本信息。返回客户ID和编码。")
    @ToolName("创建客户")
    public String createCustomer(
            @P("客户名称，必填") String name,
            @P(value = "客户简称", required = false) String shortName,
            @P(value = "客户等级，如：VIP、普通、潜在", required = false) String level,
            @P(value = "联系人姓名", required = false) String contactPerson,
            @P(value = "联系电话", required = false) String contactPhone,
            @P(value = "联系邮箱", required = false) String contactEmail,
            @P(value = "地址", required = false) String address,
            @P(value = "开户行", required = false) String bankName,
            @P(value = "银行账户", required = false) String bankAccount,
            @P(value = "税号", required = false) String taxNumber,
            @P(value = "信用额度，数字，默认0", required = false) Double creditLimit,
            @P(value = "备注", required = false) String remark) {

        try {
            log.info("创建客户: name={}, level={}, contactPerson={}", name, level, contactPerson);

            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return errorResult("客户名称不能为空");
            }

            // 生成客户编码
            String code = generateCustomerCode();

            Customer customer = new Customer();
            customer.setCode(code);
            customer.setName(name.trim());
            customer.setShortName(shortName);
            customer.setLevel(level != null ? level : "普通");
            customer.setContactPerson(contactPerson);
            customer.setContactPhone(contactPhone);
            customer.setContactEmail(contactEmail);
            customer.setAddress(address);
            customer.setBankName(bankName);
            customer.setBankAccount(bankAccount);
            customer.setTaxNumber(taxNumber);
            customer.setCreditLimit(creditLimit != null ? BigDecimal.valueOf(creditLimit) : BigDecimal.ZERO);
            customer.setCreditUsed(BigDecimal.ZERO);
            customer.setRating(3);  // 默认评级
            customer.setRemark(remark);
            customer.setStatus("ACTIVE");

            Customer created = customerService.createCustomer(customer);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", created.getId());
            result.put("code", created.getCode());
            result.put("name", created.getName());
            result.put("creditLimit", created.getCreditLimit());
            result.put("message", "客户创建成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建客户失败", e);
            return errorResult("创建客户失败: " + e.getMessage());
        }
    }

    @Tool("搜索客户。支持关键词搜索和分页。返回客户列表JSON。")
    @ToolName("搜索客户")
    public String searchCustomers(
            @P(value = "搜索关键词，可以是客户名称、编码或联系人", required = false) String keyword,
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            log.info("搜索客户: keyword={}, pageNum={}, pageSize={}", keyword, page, size);

            Page<Customer> pageResult = customerService.pageCustomers(page, size, keyword, "ACTIVE");

            List<Map<String, Object>> customers = pageResult.getRecords().stream().map(cus -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cus.getId());
                map.put("code", cus.getCode());
                map.put("name", cus.getName());
                map.put("shortName", cus.getShortName());
                map.put("level", cus.getLevel());
                map.put("contactPerson", cus.getContactPerson());
                map.put("contactPhone", cus.getContactPhone());
                map.put("creditLimit", cus.getCreditLimit());
                map.put("creditUsed", cus.getCreditUsed());
                map.put("availableCredit", cus.getCreditLimit().subtract(cus.getCreditUsed()));
                map.put("rating", cus.getRating());
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", page);
            result.put("pageSize", size);
            result.put("customers", customers);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("搜索客户失败", e);
            return errorResult("搜索客户失败: " + e.getMessage());
        }
    }

    @Tool("获取客户详情。返回客户完整信息JSON，包括信用额度使用情况。")
    @ToolName("获取客户详情")
    public String getCustomerDetail(@P("客户ID") Long customerId) {

        try {
            log.info("获取客户详情: customerId={}", customerId);

            Customer customer = customerService.getById(customerId);
            if (customer == null) {
                return errorResult("客户不存在: " + customerId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", customer.getId());
            result.put("code", customer.getCode());
            result.put("name", customer.getName());
            result.put("shortName", customer.getShortName());
            result.put("level", customer.getLevel());
            result.put("contactPerson", customer.getContactPerson());
            result.put("contactPhone", customer.getContactPhone());
            result.put("contactEmail", customer.getContactEmail());
            result.put("address", customer.getAddress());
            result.put("bankName", customer.getBankName());
            result.put("bankAccount", customer.getBankAccount());
            result.put("taxNumber", customer.getTaxNumber());
            result.put("creditLimit", customer.getCreditLimit());
            result.put("creditUsed", customer.getCreditUsed());
            result.put("availableCredit", customer.getCreditLimit().subtract(customer.getCreditUsed()));
            result.put("creditUsageRate", customer.getCreditLimit().compareTo(BigDecimal.ZERO) > 0
                    ? customer.getCreditUsed().divide(customer.getCreditLimit(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            result.put("rating", customer.getRating());
            result.put("status", customer.getStatus());
            result.put("remark", customer.getRemark());

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取客户详情失败", e);
            return errorResult("获取客户详情失败: " + e.getMessage());
        }
    }

    @Tool("查询客户信用额度。返回客户信用额度、已用额度、可用额度等信息。")
    @ToolName("查询客户信用额度")
    public String checkCustomerCredit(@P("客户ID") Long customerId) {

        try {
            log.info("查询客户信用额度: customerId={}", customerId);

            Customer customer = customerService.getById(customerId);
            if (customer == null) {
                return errorResult("客户不存在: " + customerId);
            }

            BigDecimal creditLimit = customer.getCreditLimit() != null ? customer.getCreditLimit() : BigDecimal.ZERO;
            BigDecimal creditUsed = customer.getCreditUsed() != null ? customer.getCreditUsed() : BigDecimal.ZERO;
            BigDecimal availableCredit = creditLimit.subtract(creditUsed);

            String creditStatus;
            if (creditLimit.compareTo(BigDecimal.ZERO) == 0) {
                creditStatus = "未设置信用额度";
            } else if (availableCredit.compareTo(BigDecimal.ZERO) <= 0) {
                creditStatus = "信用额度已用完";
            } else if (creditUsed.divide(creditLimit, 4, BigDecimal.ROUND_HALF_UP).compareTo(BigDecimal.valueOf(0.8)) > 0) {
                creditStatus = "信用额度使用率较高(超过80%)";
            } else {
                creditStatus = "信用额度正常";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("customerId", customer.getId());
            result.put("customerName", customer.getName());
            result.put("creditLimit", creditLimit);
            result.put("creditUsed", creditUsed);
            result.put("availableCredit", availableCredit);
            result.put("creditUsageRate", creditLimit.compareTo(BigDecimal.ZERO) > 0
                    ? creditUsed.divide(creditLimit, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            result.put("creditStatus", creditStatus);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("查询客户信用额度失败", e);
            return errorResult("查询客户信用额度失败: " + e.getMessage());
        }
    }

    private String generateCustomerCode() {
        return "CUS" + System.currentTimeMillis();
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
