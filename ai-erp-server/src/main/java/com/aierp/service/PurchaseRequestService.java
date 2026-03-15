package com.aierp.service;

import com.aierp.entity.PurchaseRequest;
import com.aierp.entity.PurchaseRequestItem;
import com.aierp.mapper.PurchaseRequestItemMapper;
import com.aierp.mapper.PurchaseRequestMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 采购申请服务
 */
@Service
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestMapper purchaseRequestMapper;
    private final PurchaseRequestItemMapper purchaseRequestItemMapper;

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
            throw new RuntimeException("采购申请不存在");
        }
        if (!"DRAFT".equals(request.getStatus())) {
            throw new RuntimeException("只有草稿状态的申请可以提交审批");
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
            throw new RuntimeException("采购申请不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("只有待审批状态的申请可以审批");
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
            throw new RuntimeException("采购申请不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("只有待审批状态的申请可以审批");
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
     * 生成申请单号
     */
    private String generateRequestNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseRequestMapper.selectCount(null);
        return "PR" + dateStr + String.format("%04d", count + 1);
    }
}
