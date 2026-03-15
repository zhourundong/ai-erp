package com.aierp.service;

import com.aierp.common.BusinessException;
import com.aierp.dto.CreateOrderFromRequest;
import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.entity.PurchaseRequest;
import com.aierp.entity.PurchaseRequestItem;
import com.aierp.mapper.PurchaseOrderItemMapper;
import com.aierp.mapper.PurchaseOrderMapper;
import com.aierp.mapper.PurchaseRequestItemMapper;
import com.aierp.mapper.PurchaseRequestMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 采购申请服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestMapper purchaseRequestMapper;
    private final PurchaseRequestItemMapper purchaseRequestItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;

    /**
     * 分页查询采购申请
     */
    public Page<PurchaseRequest> pageRequests(int pageNum, int pageSize, String keyword, String status) {
        Page<PurchaseRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(PurchaseRequest::getRequestNo, keyword)
                    .or().like(PurchaseRequest::getApplicantName, keyword)
                    .or().like(PurchaseRequest::getRequirementDescription, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PurchaseRequest::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseRequest::getCreatedAt);
        return purchaseRequestMapper.selectPage(page, wrapper);
    }

    /**
     * 获取采购申请详情（包含明细）
     */
    public PurchaseRequest getRequestById(Long id) {
        return purchaseRequestMapper.selectById(id);
    }

    /**
     * 获取采购申请明细
     */
    public List<PurchaseRequestItem> getRequestItems(Long requestId) {
        return purchaseRequestItemMapper.findByRequestId(requestId);
    }

    /**
     * 创建采购申请
     */
    @Transactional
    public PurchaseRequest createRequest(PurchaseRequest request, List<PurchaseRequestItem> items) {
        if (request.getRequestNo() == null || request.getRequestNo().isEmpty()) {
            request.setRequestNo(generateRequestNo());
        }
        if (request.getRequestDate() == null) {
            request.setRequestDate(LocalDate.now());
        }
        if (request.getStatus() == null) {
            request.setStatus("DRAFT");
        }
        if (request.getPriority() == null) {
            request.setPriority("NORMAL");
        }

        // 计算总金额
        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getEstimatedAmount() != null ? item.getEstimatedAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            request.setTotalAmount(totalAmount);
        }

        purchaseRequestMapper.insert(request);

        // 保存明细
        if (items != null && !items.isEmpty()) {
            for (PurchaseRequestItem item : items) {
                item.setRequestId(request.getId());
                if (item.getEstimatedAmount() == null && item.getEstimatedPrice() != null && item.getQuantity() != null) {
                    item.setEstimatedAmount(item.getEstimatedPrice().multiply(item.getQuantity()));
                }
                purchaseRequestItemMapper.insert(item);
            }
        }

        return request;
    }

    /**
     * 更新采购申请
     */
    @Transactional
    public PurchaseRequest updateRequest(PurchaseRequest request) {
        purchaseRequestMapper.updateById(request);
        return request;
    }

    /**
     * 添加采购申请明细
     */
    @Transactional
    public PurchaseRequestItem addRequestItem(Long requestId, PurchaseRequestItem item) {
        PurchaseRequest request = purchaseRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("采购申请不存在");
        }

        item.setRequestId(requestId);
        if (item.getEstimatedAmount() == null && item.getEstimatedPrice() != null && item.getQuantity() != null) {
            item.setEstimatedAmount(item.getEstimatedPrice().multiply(item.getQuantity()));
        }
        purchaseRequestItemMapper.insert(item);

        // 更新主表总金额
        List<PurchaseRequestItem> items = purchaseRequestItemMapper.findByRequestId(requestId);
        BigDecimal totalAmount = items.stream()
                .map(i -> i.getEstimatedAmount() != null ? i.getEstimatedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        request.setTotalAmount(totalAmount);
        purchaseRequestMapper.updateById(request);

        return item;
    }

    /**
     * 删除采购申请
     */
    @Transactional
    public void deleteRequest(Long id) {
        // 先删除明细
        LambdaQueryWrapper<PurchaseRequestItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseRequestItem::getRequestId, id);
        purchaseRequestItemMapper.delete(wrapper);
        // 再删除主表
        purchaseRequestMapper.deleteById(id);
    }

    /**
     * 提交审批
     */
    @Transactional
    public PurchaseRequest submitForApproval(Long id) {
        PurchaseRequest request = purchaseRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException("采购申请不存在");
        }
        if (!"DRAFT".equals(request.getStatus())) {
            throw new BusinessException("只有草稿状态的申请可以提交审批");
        }
        request.setStatus("PENDING");
        purchaseRequestMapper.updateById(request);
        return request;
    }

    /**
     * 审批通过
     */
    @Transactional
    public PurchaseRequest approve(Long id, Long approverId, String approverName, String comment) {
        PurchaseRequest request = purchaseRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException("采购申请不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("只有待审批状态的申请可以审批");
        }
        request.setStatus("APPROVED");
        request.setApproverId(approverId);
        request.setApproverName(approverName);
        request.setApprovalComment(comment);
        request.setApprovalTime(java.time.LocalDateTime.now());
        purchaseRequestMapper.updateById(request);
        return request;
    }

    /**
     * 审批拒绝
     */
    @Transactional
    public PurchaseRequest reject(Long id, Long approverId, String approverName, String comment) {
        PurchaseRequest request = purchaseRequestMapper.selectById(id);
        if (request == null) {
            throw new BusinessException("采购申请不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("只有待审批状态的申请可以审批");
        }
        request.setStatus("REJECTED");
        request.setApproverId(approverId);
        request.setApproverName(approverName);
        request.setApprovalComment(comment);
        request.setApprovalTime(java.time.LocalDateTime.now());
        purchaseRequestMapper.updateById(request);
        return request;
    }

    /**
     * 从采购申请生成采购订单
     *
     * @param requestId 采购申请ID
     * @param params    创建订单参数
     * @return 采购订单
     */
    @Transactional
    public PurchaseOrder createOrderFromRequest(Long requestId, CreateOrderFromRequest params) {
        log.info("从采购申请生成订单: requestId={}, supplierId={}", requestId, params.getSupplierId());

        // 1. 校验申请
        PurchaseRequest request = purchaseRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("采购申请不存在");
        }
        if (!"APPROVED".equals(request.getStatus())) {
            throw new BusinessException("只有已审批的申请可以生成订单");
        }
        if (request.getPurchaseOrderId() != null) {
            throw new BusinessException("该申请已生成订单");
        }

        // 2. 获取申请明细
        List<PurchaseRequestItem> requestItems = purchaseRequestItemMapper.findByRequestId(requestId);
        if (requestItems.isEmpty()) {
            throw new BusinessException("采购申请明细为空");
        }

        // 3. 生成采购订单
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setOrderDate(LocalDate.now());
        order.setRequestId(requestId);
        order.setSupplierId(params.getSupplierId());
        order.setSupplierName(params.getSupplierName());
        order.setBuyerId(params.getBuyerId());
        order.setBuyerName(params.getBuyerName());
        order.setStatus("DRAFT");
        order.setPaymentStatus("UNPAID");

        // 4. 处理订单明细
        List<PurchaseOrderItem> orderItems = new ArrayList<>();
        if (params.getItems() != null && !params.getItems().isEmpty()) {
            // 使用指定的明细
            Map<Long, PurchaseRequestItem> requestItemMap = requestItems.stream()
                    .collect(Collectors.toMap(PurchaseRequestItem::getId, Function.identity()));

            for (CreateOrderFromRequest.OrderItemInput input : params.getItems()) {
                PurchaseRequestItem requestItem = requestItemMap.get(input.getRequestItemId());
                if (requestItem == null) {
                    throw new BusinessException("申请明细不存在: " + input.getRequestItemId());
                }
                PurchaseOrderItem orderItem = new PurchaseOrderItem();
                orderItem.setProductId(input.getProductId() != null ? input.getProductId() : requestItem.getProductId());
                orderItem.setProductSku(input.getProductSku() != null ? input.getProductSku() : requestItem.getProductSku());
                orderItem.setProductName(input.getProductName() != null ? input.getProductName() : requestItem.getProductName());
                orderItem.setSpecification(input.getSpecification() != null ? input.getSpecification() : requestItem.getSpecification());
                orderItem.setQuantity(input.getQuantity() != null ? input.getQuantity() : requestItem.getQuantity());
                orderItem.setUnit(input.getUnit() != null ? input.getUnit() : requestItem.getUnit());
                orderItem.setUnitPrice(input.getUnitPrice() != null ? input.getUnitPrice() : requestItem.getEstimatedPrice());
                orderItem.setStatus("PENDING");
                if (orderItem.getUnitPrice() != null && orderItem.getQuantity() != null) {
                    orderItem.setAmount(orderItem.getUnitPrice().multiply(orderItem.getQuantity()));
                }
                orderItems.add(orderItem);
            }
        } else {
            // 使用全部申请明细
            for (PurchaseRequestItem requestItem : requestItems) {
                PurchaseOrderItem orderItem = new PurchaseOrderItem();
                orderItem.setProductId(requestItem.getProductId());
                orderItem.setProductSku(requestItem.getProductSku());
                orderItem.setProductName(requestItem.getProductName());
                orderItem.setSpecification(requestItem.getSpecification());
                orderItem.setQuantity(requestItem.getQuantity());
                orderItem.setUnit(requestItem.getUnit());
                orderItem.setUnitPrice(requestItem.getEstimatedPrice());
                orderItem.setStatus("PENDING");
                if (orderItem.getUnitPrice() != null && orderItem.getQuantity() != null) {
                    orderItem.setAmount(orderItem.getUnitPrice().multiply(orderItem.getQuantity()));
                }
                orderItems.add(orderItem);
            }
        }

        // 计算总金额
        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);

        // 5. 保存订单
        purchaseOrderMapper.insert(order);

        for (PurchaseOrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            purchaseOrderItemMapper.insert(orderItem);
        }

        // 6. 更新申请关联订单ID
        request.setPurchaseOrderId(order.getId());
        purchaseRequestMapper.updateById(request);

        log.info("采购订单生成成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        return order;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderMapper.selectCount(null);
        return "PO" + dateStr + String.format("%04d", count + 1);
    }

    /**
     * 生成申请单号
     */
    private String generateRequestNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseRequestMapper.selectCount(null);
        return "PR" + dateStr + String.format("%04d", count + 1);
    }
}
