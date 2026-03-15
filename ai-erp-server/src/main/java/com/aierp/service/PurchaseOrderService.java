package com.aierp.service;

import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.mapper.PurchaseOrderItemMapper;
import com.aierp.mapper.PurchaseOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;

    public Page<PurchaseOrder> pageOrders(int pageNum, int pageSize, String keyword, String status) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(PurchaseOrder::getOrderNo, keyword)
                    .or().like(PurchaseOrder::getSupplierName, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreatedAt);
        return purchaseOrderMapper.selectPage(page, wrapper);
    }

    public PurchaseOrder getById(Long id) {
        return purchaseOrderMapper.selectById(id);
    }

    public List<PurchaseOrderItem> getOrderItems(Long orderId) {
        return purchaseOrderItemMapper.findByOrderId(orderId);
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrder order, List<PurchaseOrderItem> items) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (order.getStatus() == null) {
            order.setStatus("DRAFT");
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("UNPAID");
        }

        // 计算金额
        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
        }

        purchaseOrderMapper.insert(order);

        // 保存明细
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                item.setOrderId(order.getId());
                if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
                }
                if (item.getStatus() == null) {
                    item.setStatus("PENDING");
                }
                purchaseOrderItemMapper.insert(item);
            }
        }

        return order;
    }

    @Transactional
    public PurchaseOrder updateOrder(PurchaseOrder order) {
        purchaseOrderMapper.updateById(order);
        return order;
    }

    @Transactional
    public void deleteOrder(Long id) {
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getOrderId, id);
        purchaseOrderItemMapper.delete(wrapper);
        purchaseOrderMapper.deleteById(id);
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderMapper.selectCount(null);
        return "PO" + dateStr + String.format("%04d", count + 1);
    }
}
