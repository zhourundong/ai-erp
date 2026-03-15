package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.PurchaseRequest;
import com.aierp.entity.PurchaseRequestItem;
import com.aierp.service.PurchaseRequestService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 采购申请控制器
 */
@RestController
@RequestMapping("/api/purchase/requests")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @GetMapping
    public Result<Page<PurchaseRequest>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(purchaseRequestService.pageRequests(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRequest> get(@PathVariable Long id) {
        return Result.success(purchaseRequestService.getRequestById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<PurchaseRequestItem>> getItems(@PathVariable Long id) {
        return Result.success(purchaseRequestService.getRequestItems(id));
    }

    @PostMapping
    public Result<PurchaseRequest> create(@RequestBody Map<String, Object> params) {
        PurchaseRequest request = convertToRequest(params);
        List<PurchaseRequestItem> items = convertToItems(params);
        return Result.success(purchaseRequestService.createRequest(request, items));
    }

    @PutMapping("/{id}")
    public Result<PurchaseRequest> update(@PathVariable Long id, @RequestBody PurchaseRequest request) {
        request.setId(id);
        return Result.success(purchaseRequestService.updateRequest(request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseRequestService.deleteRequest(id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<PurchaseRequest> submit(@PathVariable Long id) {
        return Result.success(purchaseRequestService.submitForApproval(id));
    }

    @PostMapping("/{id}/approve")
    public Result<PurchaseRequest> approve(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam String approverName,
            @RequestParam(required = false) String comment) {
        return Result.success(purchaseRequestService.approve(id, approverId, approverName, comment));
    }

    @PostMapping("/{id}/reject")
    public Result<PurchaseRequest> reject(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam String approverName,
            @RequestParam(required = false) String comment) {
        return Result.success(purchaseRequestService.reject(id, approverId, approverName, comment));
    }

    @SuppressWarnings("unchecked")
    private PurchaseRequest convertToRequest(Map<String, Object> params) {
        PurchaseRequest request = new PurchaseRequest();
        if (params.get("applicantId") != null) request.setApplicantId(Long.valueOf(params.get("applicantId").toString()));
        if (params.get("applicantName") != null) request.setApplicantName(params.get("applicantName").toString());
        if (params.get("department") != null) request.setDepartment(params.get("department").toString());
        if (params.get("requirementDescription") != null) request.setRequirementDescription(params.get("requirementDescription").toString());
        if (params.get("priority") != null) request.setPriority(params.get("priority").toString());
        return request;
    }

    @SuppressWarnings("unchecked")
    private List<PurchaseRequestItem> convertToItems(Map<String, Object> params) {
        if (params.get("items") == null) return null;
        return ((List<Map<String, Object>>) params.get("items")).stream().map(item -> {
            PurchaseRequestItem requestItem = new PurchaseRequestItem();
            if (item.get("productId") != null) requestItem.setProductId(Long.valueOf(item.get("productId").toString()));
            if (item.get("productSku") != null) requestItem.setProductSku(item.get("productSku").toString());
            if (item.get("productName") != null) requestItem.setProductName(item.get("productName").toString());
            if (item.get("specification") != null) requestItem.setSpecification(item.get("specification").toString());
            if (item.get("category") != null) requestItem.setCategory(item.get("category").toString());
            if (item.get("quantity") != null) requestItem.setQuantity(new java.math.BigDecimal(item.get("quantity").toString()));
            if (item.get("unit") != null) requestItem.setUnit(item.get("unit").toString());
            if (item.get("estimatedPrice") != null) requestItem.setEstimatedPrice(new java.math.BigDecimal(item.get("estimatedPrice").toString()));
            if (item.get("remark") != null) requestItem.setRemark(item.get("remark").toString());
            return requestItem;
        }).toList();
    }
}
