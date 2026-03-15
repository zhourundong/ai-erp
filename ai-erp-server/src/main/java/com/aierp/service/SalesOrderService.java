package com.aierp.service;

import com.aierp.entity.SalesOrder;
import com.aierp.entity.SalesOrderItem;
import com.aierp.mapper.SalesOrderItemMapper;
import com.aierp.mapper.SalesOrderMapper;
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
public class SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;

    public Page<SalesOrder> pageOrders(int pageNum, int pageSize, String keyword, String status) {
        Page<SalesOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(SalesOrder::getOrderNo, keyword)
                    .or().like(SalesOrder::getCustomerName, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SalesOrder::getStatus, status);
        }
        wrapper.orderByDesc(SalesOrder::getCreatedAt);
        return salesOrderMapper.selectPage(page, wrapper);
    }

    public SalesOrder getById(Long id) {
        return salesOrderMapper.selectById(id);
    }

    public List<SalesOrderItem> getOrderItems(Long orderId) {
        return salesOrderItemMapper.findByOrderId(orderId);
    }

    @Transactional
    public SalesOrder createOrder(SalesOrder order, List<SalesOrderItem> items) {
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

        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
        }

        salesOrderMapper.insert(order);

        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem item : items) {
                item.setOrderId(order.getId());
                if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
                }
                if (item.getStatus() == null) {
                    item.setStatus("PENDING");
                }
                salesOrderItemMapper.insert(item);
            }
        }

        return order;
    }

    @Transactional
    public SalesOrder updateOrder(SalesOrder order) {
        salesOrderMapper.updateById(order);
        return order;
    }

    @Transactional
    public void deleteOrder(Long id) {
        LambdaQueryWrapper<SalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderItem::getOrderId, id);
        salesOrderItemMapper.delete(wrapper);
        salesOrderMapper.deleteById(id);
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = salesOrderMapper.selectCount(null);
        return "SO" + dateStr + String.format("%04d", count + 1);
    }
}
