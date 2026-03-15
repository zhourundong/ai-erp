package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.dto.ShipRequest;
import com.aierp.entity.SalesOrder;
import com.aierp.entity.SalesOrderItem;
import com.aierp.service.SalesOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public Result<Page<SalesOrder>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(salesOrderService.pageOrders(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<SalesOrder> get(@PathVariable Long id) {
        return Result.success(salesOrderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<SalesOrderItem>> getItems(@PathVariable Long id) {
        return Result.success(salesOrderService.getOrderItems(id));
    }

    @PostMapping
    public Result<SalesOrder> create(@RequestBody Map<String, Object> params) {
        SalesOrder order = convertToOrder(params);
        List<SalesOrderItem> items = convertToItems(params);
        return Result.success(salesOrderService.createOrder(order, items));
    }

    @PutMapping("/{id}")
    public Result<SalesOrder> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        SalesOrder order = convertToOrder(params);
        order.setId(id);
        List<SalesOrderItem> items = convertToItems(params);
        return Result.success(salesOrderService.updateOrderWithItems(order, items));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        salesOrderService.deleteOrder(id);
        return Result.success();
    }

    /**
     * 确认订单（检查库存）
     */
    @PostMapping("/{id}/confirm")
    public Result<SalesOrder> confirm(@PathVariable Long id, @RequestParam Long warehouseId) {
        return Result.success(salesOrderService.confirm(id, warehouseId));
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<SalesOrder> cancel(@PathVariable Long id) {
        return Result.success(salesOrderService.cancel(id));
    }

    /**
     * 反确认订单
     */
    @PostMapping("/{id}/unconfirm")
    public Result<SalesOrder> unconfirm(@PathVariable Long id) {
        return Result.success(salesOrderService.unconfirm(id));
    }

    /**
     * 发货出库
     */
    @PostMapping("/{id}/ship")
    public Result<SalesOrder> ship(@PathVariable Long id, @RequestBody ShipRequest request) {
        return Result.success(salesOrderService.shipGoods(
                id,
                request.getWarehouseId(),
                request.getItems(),
                request.getOperator()
        ));
    }

    @SuppressWarnings("unchecked")
    private SalesOrder convertToOrder(Map<String, Object> params) {
        SalesOrder order = new SalesOrder();
        if (params.get("customerId") != null) order.setCustomerId(Long.valueOf(params.get("customerId").toString()));
        if (params.get("customerName") != null) order.setCustomerName(params.get("customerName").toString());
        if (params.get("salesId") != null) order.setSalesId(Long.valueOf(params.get("salesId").toString()));
        if (params.get("salesName") != null) order.setSalesName(params.get("salesName").toString());
        if (params.get("remark") != null) order.setRemark(params.get("remark").toString());
        return order;
    }

    @SuppressWarnings("unchecked")
    private List<SalesOrderItem> convertToItems(Map<String, Object> params) {
        if (params.get("items") == null) return null;
        return ((List<Map<String, Object>>) params.get("items")).stream().map(item -> {
            SalesOrderItem orderItem = new SalesOrderItem();
            if (item.get("productId") != null) orderItem.setProductId(Long.valueOf(item.get("productId").toString()));
            if (item.get("productSku") != null) orderItem.setProductSku(item.get("productSku").toString());
            if (item.get("productName") != null) orderItem.setProductName(item.get("productName").toString());
            if (item.get("specification") != null) orderItem.setSpecification(item.get("specification").toString());
            if (item.get("quantity") != null) orderItem.setQuantity(new java.math.BigDecimal(item.get("quantity").toString()));
            if (item.get("unit") != null) orderItem.setUnit(item.get("unit").toString());
            if (item.get("unitPrice") != null) orderItem.setUnitPrice(new java.math.BigDecimal(item.get("unitPrice").toString()));
            return orderItem;
        }).toList();
    }
}
