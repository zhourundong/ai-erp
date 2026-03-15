package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.dto.ReceiveRequest;
import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.service.PurchaseOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public Result<Page<PurchaseOrder>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(purchaseOrderService.pageOrders(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> get(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<PurchaseOrderItem>> getItems(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getOrderItems(id));
    }

    @PostMapping
    public Result<PurchaseOrder> create(@RequestBody Map<String, Object> params) {
        PurchaseOrder order = convertToOrder(params);
        List<PurchaseOrderItem> items = convertToItems(params);
        return Result.success(purchaseOrderService.createOrder(order, items));
    }

    @PutMapping("/{id}")
    public Result<PurchaseOrder> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        PurchaseOrder order = convertToOrder(params);
        order.setId(id);
        List<PurchaseOrderItem> items = convertToItems(params);
        return Result.success(purchaseOrderService.updateOrderWithItems(order, items));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
        return Result.success();
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit")
    public Result<PurchaseOrder> submit(@PathVariable Long id) {
        return Result.success(purchaseOrderService.submitForApproval(id));
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<PurchaseOrder> approve(@PathVariable Long id) {
        return Result.success(purchaseOrderService.approve(id));
    }

    /**
     * 审批拒绝
     */
    @PostMapping("/{id}/reject")
    public Result<PurchaseOrder> reject(@PathVariable Long id) {
        return Result.success(purchaseOrderService.reject(id));
    }

    /**
     * 反审核
     */
    @PostMapping("/{id}/unapprove")
    public Result<PurchaseOrder> unapprove(@PathVariable Long id) {
        return Result.success(purchaseOrderService.unapprove(id));
    }

    /**
     * 收货入库
     */
    @PostMapping("/{id}/receive")
    public Result<PurchaseOrder> receive(@PathVariable Long id, @RequestBody ReceiveRequest request) {
        return Result.success(purchaseOrderService.receiveGoods(
                id,
                request.getWarehouseId(),
                request.getItems(),
                request.getOperator()
        ));
    }

    @SuppressWarnings("unchecked")
    private PurchaseOrder convertToOrder(Map<String, Object> params) {
        PurchaseOrder order = new PurchaseOrder();
        if (params.get("supplierId") != null) order.setSupplierId(Long.valueOf(params.get("supplierId").toString()));
        if (params.get("supplierName") != null) order.setSupplierName(params.get("supplierName").toString());
        if (params.get("buyerId") != null) order.setBuyerId(Long.valueOf(params.get("buyerId").toString()));
        if (params.get("buyerName") != null) order.setBuyerName(params.get("buyerName").toString());
        if (params.get("remark") != null) order.setRemark(params.get("remark").toString());
        return order;
    }

    @SuppressWarnings("unchecked")
    private List<PurchaseOrderItem> convertToItems(Map<String, Object> params) {
        if (params.get("items") == null) return null;
        return ((List<Map<String, Object>>) params.get("items")).stream().map(item -> {
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
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
