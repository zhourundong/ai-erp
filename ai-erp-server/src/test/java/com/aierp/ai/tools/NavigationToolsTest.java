package com.aierp.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NavigationTools 单元测试
 */
class NavigationToolsTest {

    private NavigationTools navigationTools;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        navigationTools = new NavigationTools();
    }

    @Test
    @DisplayName("导航到采购订单列表")
    void testNavigateToPurchaseOrders() throws Exception {
        String result = navigationTools.navigate("purchase-orders", null);

        assertNotNull(result);
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"action\":\"navigate\""));
        assertTrue(result.contains("\"path\":\"/purchase-orders\""));
        assertTrue(result.contains("\"requiresConfirmation\":false"));
    }

    @Test
    @DisplayName("导航到库存页面")
    void testNavigateToInventory() throws Exception {
        String result = navigationTools.navigate("inventory", null);

        assertNotNull(result);
        assertTrue(result.contains("\"path\":\"/inventory\""));
    }

    @Test
    @DisplayName("导航带过滤条件")
    void testNavigateWithFilter() throws Exception {
        String result = navigationTools.navigate("purchase-orders", "{\"status\":\"PENDING\"}");

        assertNotNull(result);
        assertTrue(result.contains("\"success\":true"));

        // 解析并验证filter
        var rootNode = objectMapper.readTree(result);
        var navigation = rootNode.get("navigation");
        assertNotNull(navigation);
        var filter = navigation.get("filter");
        assertNotNull(filter);
        assertEquals("PENDING", filter.get("status").asText());
    }

    @Test
    @DisplayName("导航带关键词过滤")
    void testNavigateWithKeywordFilter() throws Exception {
        String result = navigationTools.navigate("products", "笔记本");

        assertNotNull(result);
        var rootNode = objectMapper.readTree(result);
        var filter = rootNode.get("navigation").get("filter");
        assertNotNull(filter);
        assertEquals("笔记本", filter.get("keyword").asText());
    }

    @Test
    @DisplayName("导航到未知页面")
    void testNavigateToUnknownPage() throws Exception {
        String result = navigationTools.navigate("unknown-page", null);

        assertNotNull(result);
        assertTrue(result.contains("\"path\":\"/unknown-page\""));
    }

    @Test
    @DisplayName("打开创建采购订单表单")
    void testOpenCreatePurchaseOrderForm() throws Exception {
        String result = navigationTools.openCreateForm("purchase-order");

        assertNotNull(result);
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"action\":\"openCreateForm\""));
        assertTrue(result.contains("\"path\":\"/purchase-orders?create=true\""));
        assertTrue(result.contains("\"requiresConfirmation\":true"));
        assertTrue(result.contains("\"confirmText\":\"立即创建\""));
    }

    @Test
    @DisplayName("打开创建商品表单")
    void testOpenCreateProductForm() throws Exception {
        String result = navigationTools.openCreateForm("product");

        assertNotNull(result);
        assertTrue(result.contains("\"path\":\"/products?create=true\""));
    }

    @Test
    @DisplayName("打开创建未知表单类型")
    void testOpenCreateUnknownForm() throws Exception {
        String result = navigationTools.openCreateForm("unknown-form");

        assertNotNull(result);
        assertTrue(result.contains("\"path\":\"/unknown-form?create=true\""));
    }

    @Test
    @DisplayName("打开采购订单详情")
    void testOpenPurchaseOrderDetail() throws Exception {
        String result = navigationTools.openDetail("purchase-order", 123L);

        assertNotNull(result);
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"path\":\"/purchase-orders/123\""));
        assertTrue(result.contains("\"recordId\":123"));
        assertTrue(result.contains("\"requiresConfirmation\":false"));
    }

    @Test
    @DisplayName("打开商品详情")
    void testOpenProductDetail() throws Exception {
        String result = navigationTools.openDetail("product", 456L);

        assertNotNull(result);
        assertTrue(result.contains("\"path\":\"/products/456\""));
    }

    @Test
    @DisplayName("打开详情 - 无效ID")
    void testOpenDetailWithInvalidId() throws Exception {
        String result = navigationTools.openDetail("product", 0L);

        assertNotNull(result);
        assertTrue(result.contains("\"success\":false"));
        assertTrue(result.contains("\"error\""));
    }

    @Test
    @DisplayName("打开详情 - 空ID")
    void testOpenDetailWithNullId() throws Exception {
        String result = navigationTools.openDetail("product", null);

        assertNotNull(result);
        assertTrue(result.contains("\"success\":false"));
    }

    @Test
    @DisplayName("打开详情 - 未知记录类型")
    void testOpenDetailWithUnknownType() throws Exception {
        String result = navigationTools.openDetail("unknown-type", 123L);

        assertNotNull(result);
        assertTrue(result.contains("\"path\":\"/unknown-type/123\""));
    }

    @Test
    @DisplayName("验证页面描述正确")
    void testPageDescription() throws Exception {
        String result = navigationTools.navigate("purchase-orders", null);
        assertTrue(result.contains("采购订单列表"));

        result = navigationTools.navigate("sales-orders", null);
        assertTrue(result.contains("销售订单列表"));

        result = navigationTools.navigate("inventory", null);
        assertTrue(result.contains("库存查询"));

        result = navigationTools.navigate("products", null);
        assertTrue(result.contains("商品管理"));
    }

    @Test
    @DisplayName("验证表单描述正确")
    void testFormDescription() throws Exception {
        String result = navigationTools.openCreateForm("purchase-order");
        assertTrue(result.contains("创建采购订单"));

        result = navigationTools.openCreateForm("sales-order");
        assertTrue(result.contains("创建销售订单"));

        result = navigationTools.openCreateForm("product");
        assertTrue(result.contains("创建商品"));
    }
}
