package com.aierp.ai.tools;

import com.aierp.entity.Product;
import com.aierp.service.ProductService;
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
 * 商品相关工具
 *
 * 提供商品查询、搜索、新增等操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {

    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("创建商品。需要商品基本信息，SKU编码不填则自动生成。返回商品ID和SKU。")
    @ToolName("创建商品")
    public String createProduct(
            @P(value = "商品SKU编码，不填则自动生成", required = false) String sku,
            @P("商品名称，必填") String name,
            @P(value = "商品类别名称，如：办公用品、电子设备、原材料", required = false) String categoryName,
            @P(value = "品牌", required = false) String brand,
            @P(value = "型号", required = false) String model,
            @P(value = "规格型号", required = false) String specification,
            @P(value = "单位，如：个、件、箱、米、千克", required = false) String unit,
            @P(value = "成本价，数字", required = false) Double costPrice,
            @P(value = "销售价，数字", required = false) Double salePrice,
            @P(value = "市场价，数字", required = false) Double marketPrice,
            @P(value = "安全库存，整数", required = false) Integer safetyStock,
            @P(value = "最小订货量，整数", required = false) Integer minOrderQty,
            @P(value = "商品描述", required = false) String description) {

        try {
            log.info("创建商品: sku={}, name={}, categoryName={}", sku, name, categoryName);

            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return errorResult("商品名称不能为空");
            }

            // 处理SKU：不为空时检查是否已存在
            if (sku != null && !sku.trim().isEmpty()) {
                Product existing = productService.getBySku(sku.trim());
                if (existing != null) {
                    return errorResult("商品SKU已存在: " + sku);
                }
            }

            Product product = new Product();
            product.setSku(sku != null && !sku.trim().isEmpty() ? sku.trim() : null);
            product.setName(name.trim());
            product.setCategoryName(categoryName);
            product.setBrand(brand);
            product.setModel(model);
            product.setSpecification(specification);
            product.setUnit(unit != null ? unit : "个");
            product.setCostPrice(costPrice != null ? BigDecimal.valueOf(costPrice) : null);
            product.setSalePrice(salePrice != null ? BigDecimal.valueOf(salePrice) : null);
            product.setMarketPrice(marketPrice != null ? BigDecimal.valueOf(marketPrice) : null);
            product.setSafetyStock(safetyStock != null ? safetyStock : 0);
            product.setMinOrderQty(minOrderQty != null ? minOrderQty : 1);
            product.setDescription(description);
            product.setStatus("ACTIVE");

            Product created = productService.createProduct(product);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", created.getId());
            result.put("sku", created.getSku());
            result.put("name", created.getName());
            result.put("unit", created.getUnit());
            result.put("message", "商品创建成功");

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建商品失败", e);
            return errorResult("创建商品失败: " + e.getMessage());
        }
    }

    @Tool("搜索商品。支持关键词搜索和分页。返回商品列表JSON。")
    @ToolName("搜索商品")
    public String searchProducts(
            @P(value = "搜索关键词，可以是商品名称、SKU或品牌", required = false) String keyword,
            @P(value = "页码，从1开始，默认1", required = false) Integer pageNum,
            @P(value = "每页数量，默认10", required = false) Integer pageSize) {

        try {
            int page = pageNum != null ? pageNum : 1;
            int size = pageSize != null ? pageSize : 10;

            log.info("搜索商品: keyword={}, pageNum={}, pageSize={}", keyword, page, size);

            Page<Product> pageResult = productService.pageProducts(page, size, keyword, "ACTIVE", null);

            List<Map<String, Object>> products = pageResult.getRecords().stream().map(prod -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", prod.getId());
                map.put("sku", prod.getSku());
                map.put("name", prod.getName());
                map.put("categoryName", prod.getCategoryName());
                map.put("brand", prod.getBrand());
                map.put("model", prod.getModel());
                map.put("specification", prod.getSpecification());
                map.put("unit", prod.getUnit());
                map.put("costPrice", prod.getCostPrice());
                map.put("salePrice", prod.getSalePrice());
                map.put("marketPrice", prod.getMarketPrice());
                map.put("safetyStock", prod.getSafetyStock());
                map.put("status", prod.getStatus());
                return map;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", page);
            result.put("pageSize", size);
            result.put("products", products);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("搜索商品失败", e);
            return errorResult("搜索商品失败: " + e.getMessage());
        }
    }

    @Tool("根据SKU编码查询商品详情。返回商品完整信息JSON。")
    @ToolName("根据SKU查询商品")
    public String getProductBySku(@P("商品SKU编码") String sku) {

        try {
            log.info("根据SKU查询商品: sku={}", sku);

            Product product = productService.getBySku(sku);
            if (product == null) {
                return errorResult("商品不存在: " + sku);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", product.getId());
            result.put("sku", product.getSku());
            result.put("name", product.getName());
            result.put("categoryId", product.getCategoryId());
            result.put("categoryName", product.getCategoryName());
            result.put("brand", product.getBrand());
            result.put("model", product.getModel());
            result.put("specification", product.getSpecification());
            result.put("unit", product.getUnit());
            result.put("costPrice", product.getCostPrice());
            result.put("salePrice", product.getSalePrice());
            result.put("marketPrice", product.getMarketPrice());
            result.put("safetyStock", product.getSafetyStock());
            result.put("maxStock", product.getMaxStock());
            result.put("minOrderQty", product.getMinOrderQty());
            result.put("barcode", product.getBarcode());
            result.put("imageUrl", product.getImageUrl());
            result.put("status", product.getStatus());
            result.put("description", product.getDescription());

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("查询商品详情失败", e);
            return errorResult("查询商品详情失败: " + e.getMessage());
        }
    }

    @Tool("根据ID查询商品详情。返回商品完整信息JSON。")
    @ToolName("根据ID查询商品")
    public String getProductById(@P("商品ID") Long productId) {

        try {
            log.info("根据ID查询商品: productId={}", productId);

            Product product = productService.getById(productId);
            if (product == null) {
                return errorResult("商品不存在: " + productId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", product.getId());
            result.put("sku", product.getSku());
            result.put("name", product.getName());
            result.put("categoryId", product.getCategoryId());
            result.put("categoryName", product.getCategoryName());
            result.put("brand", product.getBrand());
            result.put("model", product.getModel());
            result.put("specification", product.getSpecification());
            result.put("unit", product.getUnit());
            result.put("costPrice", product.getCostPrice());
            result.put("salePrice", product.getSalePrice());
            result.put("marketPrice", product.getMarketPrice());
            result.put("safetyStock", product.getSafetyStock());
            result.put("maxStock", product.getMaxStock());
            result.put("minOrderQty", product.getMinOrderQty());
            result.put("barcode", product.getBarcode());
            result.put("imageUrl", product.getImageUrl());
            result.put("status", product.getStatus());
            result.put("description", product.getDescription());

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("查询商品详情失败", e);
            return errorResult("查询商品详情失败: " + e.getMessage());
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
