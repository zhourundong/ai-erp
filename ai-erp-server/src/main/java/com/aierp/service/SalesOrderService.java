package com.aierp.service;

import com.aierp.common.BusinessException;
import com.aierp.dto.ShipRequest;
import com.aierp.entity.Inventory;
import com.aierp.entity.SalesOrder;
import com.aierp.entity.SalesOrderItem;
import com.aierp.entity.Warehouse;
import com.aierp.mapper.SalesOrderItemMapper;
import com.aierp.mapper.SalesOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;

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

        salesOrderMapper.insert(order);

        // 保存明细并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem item : items) {
                item.setOrderId(order.getId());
                // 先计算明细金额
                if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
                }
                if (item.getStatus() == null) {
                    item.setStatus("PENDING");
                }
                salesOrderItemMapper.insert(item);
                // 累加到总金额
                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }
            }
            // 设置订单总金额
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            salesOrderMapper.updateById(order);
        }

        return order;
    }

    @Transactional
    public SalesOrder updateOrder(SalesOrder order) {
        salesOrderMapper.updateById(order);
        return order;
    }

    /**
     * 更新订单及明细（仅草稿状态）
     */
    @Transactional
    public SalesOrder updateOrderWithItems(SalesOrder order, List<SalesOrderItem> items) {
        SalesOrder existingOrder = salesOrderMapper.selectById(order.getId());
        if (existingOrder == null) {
            throw new BusinessException("销售订单不存在");
        }
        if (!"DRAFT".equals(existingOrder.getStatus())) {
            throw new BusinessException("只有草稿状态的订单可以编辑");
        }

        // 删除原明细
        LambdaQueryWrapper<SalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrderItem::getOrderId, order.getId());
        salesOrderItemMapper.delete(wrapper);

        // 计算金额并保存新明细
        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (SalesOrderItem item : items) {
                item.setOrderId(order.getId());
                if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
                }
                if (item.getStatus() == null) {
                    item.setStatus("PENDING");
                }
                totalAmount = totalAmount.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
                salesOrderItemMapper.insert(item);
            }
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
        }

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

    /**
     * 销售订单确认
     */
    @Transactional
    public SalesOrder confirm(Long id, Long warehouseId) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        if (!"DRAFT".equals(order.getStatus())) {
            throw new BusinessException("只有草稿状态的订单可以确认");
        }

        // 检查库存
        List<SalesOrderItem> items = salesOrderItemMapper.findByOrderId(id);
        for (SalesOrderItem item : items) {
            Inventory inv = inventoryService.getByWarehouseAndProduct(warehouseId, item.getProductId());
            if (inv == null || inv.getAvailableQty().compareTo(item.getQuantity()) < 0) {
                throw new BusinessException("商品 [" + item.getProductName() + "] 库存不足，当前库存: "
                        + (inv != null ? inv.getAvailableQty() : "0"));
            }
        }

        order.setStatus("CONFIRMED");
        salesOrderMapper.updateById(order);
        return order;
    }

    /**
     * 销售订单取消
     */
    @Transactional
    public SalesOrder cancel(Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        if ("COMPLETED".equals(order.getStatus()) || "SHIPPING".equals(order.getStatus())) {
            throw new BusinessException("已完成或发货中的订单不能取消");
        }
        order.setStatus("CANCELLED");
        salesOrderMapper.updateById(order);
        return order;
    }

    /**
     * 销售订单反确认
     */
    @Transactional
    public SalesOrder unconfirm(Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("只有已确认状态的订单可以反确认");
        }
        order.setStatus("DRAFT");
        salesOrderMapper.updateById(order);
        return order;
    }

    /**
     * 销售发货出库
     *
     * @param orderId     销售订单ID
     * @param warehouseId 出库仓库ID
     * @param items       发货明细
     * @param operator    操作人
     * @return 更新后的订单
     */
    @Transactional
    public SalesOrder shipGoods(Long orderId, Long warehouseId,
                                List<ShipRequest.ShipItem> items, String operator) {
        log.info("销售发货出库: orderId={}, warehouseId={}, items={}", orderId, warehouseId, items);

        // 1. 校验订单
        SalesOrder order = salesOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("销售订单不存在");
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"SHIPPING".equals(order.getStatus())) {
            throw new BusinessException("只有已确认或发货中的订单可以发货出库");
        }

        // 2. 校验仓库
        Warehouse warehouse = warehouseService.getById(warehouseId);
        if (warehouse == null || !"ACTIVE".equals(warehouse.getStatus())) {
            throw new BusinessException("仓库不存在或已停用");
        }

        // 3. 获取订单明细并建立映射
        List<SalesOrderItem> orderItems = salesOrderItemMapper.findByOrderId(orderId);
        Map<Long, SalesOrderItem> itemMap = orderItems.stream()
                .collect(Collectors.toMap(SalesOrderItem::getId, Function.identity()));

        // 4. 遍历发货明细，执行出库
        boolean allCompleted = true;
        for (ShipRequest.ShipItem shipItem : items) {
            SalesOrderItem orderItem = itemMap.get(shipItem.getItemId());
            if (orderItem == null) {
                throw new BusinessException("订单明细不存在: " + shipItem.getItemId());
            }

            // 校验发货数量不能超过订单数量
            BigDecimal newShippedQty = (orderItem.getShippedQty() != null ? orderItem.getShippedQty() : BigDecimal.ZERO)
                    .add(shipItem.getQuantity());
            if (newShippedQty.compareTo(orderItem.getQuantity()) > 0) {
                throw new BusinessException("发货数量超过订单数量: " + orderItem.getProductName());
            }

            // 执行出库
            inventoryService.stockOut(
                    warehouseId,
                    orderItem.getProductId(),
                    shipItem.getQuantity(),
                    "SALES_OUT",
                    orderItem.getUnitPrice(),
                    "SALES_ORDER",
                    orderId,
                    order.getOrderNo(),
                    operator
            );

            // 更新明细已发货数量
            orderItem.setShippedQty(newShippedQty);
            if (newShippedQty.compareTo(orderItem.getQuantity()) == 0) {
                orderItem.setStatus("SHIPPED");
            } else {
                orderItem.setStatus("PARTIAL");
                allCompleted = false;
            }
            salesOrderItemMapper.updateById(orderItem);
        }

        // 5. 更新订单状态
        if (allCompleted) {
            order.setStatus("COMPLETED");
            order.setActualDeliveryDate(LocalDate.now());
        } else {
            order.setStatus("SHIPPING");
        }
        salesOrderMapper.updateById(order);

        log.info("销售发货出库完成: orderId={}, status={}", orderId, order.getStatus());
        return order;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = salesOrderMapper.selectCount(null);
        return "SO" + dateStr + String.format("%04d", count + 1);
    }
}
