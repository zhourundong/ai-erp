package com.aierp.service;

import com.aierp.common.BusinessException;
import com.aierp.dto.ReceiveRequest;
import com.aierp.entity.Inventory;
import com.aierp.entity.PurchaseOrder;
import com.aierp.entity.PurchaseOrderItem;
import com.aierp.entity.Warehouse;
import com.aierp.mapper.PurchaseOrderItemMapper;
import com.aierp.mapper.PurchaseOrderMapper;
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
public class PurchaseOrderService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;

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

        purchaseOrderMapper.insert(order);

        // 保存明细并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                item.setOrderId(order.getId());
                // 先计算明细金额
                if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
                }
                if (item.getStatus() == null) {
                    item.setStatus("PENDING");
                }
                purchaseOrderItemMapper.insert(item);
                // 累加到总金额
                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }
            }
            // 设置订单总金额
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            purchaseOrderMapper.updateById(order);
        }

        return order;
    }

    @Transactional
    public PurchaseOrder updateOrder(PurchaseOrder order) {
        purchaseOrderMapper.updateById(order);
        return order;
    }

    /**
     * 更新订单及明细（仅草稿状态）
     */
    @Transactional
    public PurchaseOrder updateOrderWithItems(PurchaseOrder order, List<PurchaseOrderItem> items) {
        PurchaseOrder existingOrder = purchaseOrderMapper.selectById(order.getId());
        if (existingOrder == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"DRAFT".equals(existingOrder.getStatus())) {
            throw new BusinessException("只有草稿状态的订单可以编辑");
        }

        // 删除原明细
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getOrderId, order.getId());
        purchaseOrderItemMapper.delete(wrapper);

        // 计算金额并保存新明细
        if (items != null && !items.isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PurchaseOrderItem item : items) {
                item.setOrderId(order.getId());
                if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                    item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
                }
                if (item.getStatus() == null) {
                    item.setStatus("PENDING");
                }
                totalAmount = totalAmount.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
                purchaseOrderItemMapper.insert(item);
            }
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
        }

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

    /**
     * 采购订单提交审批
     */
    @Transactional
    public PurchaseOrder submitForApproval(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"DRAFT".equals(order.getStatus())) {
            throw new BusinessException("只有草稿状态的订单可以提交审批");
        }
        order.setStatus("PENDING");
        purchaseOrderMapper.updateById(order);
        return order;
    }

    /**
     * 采购订单审批通过
     */
    @Transactional
    public PurchaseOrder approve(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待审批状态的订单可以审批");
        }
        order.setStatus("APPROVED");
        purchaseOrderMapper.updateById(order);
        return order;
    }

    /**
     * 采购订单审批拒绝
     */
    @Transactional
    public PurchaseOrder reject(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待审批状态的订单可以审批");
        }
        order.setStatus("REJECTED");
        purchaseOrderMapper.updateById(order);
        return order;
    }

    /**
     * 采购订单反审核
     */
    @Transactional
    public PurchaseOrder unapprove(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"APPROVED".equals(order.getStatus())) {
            throw new BusinessException("只有已审批状态的订单可以反审核");
        }
        order.setStatus("PENDING");
        purchaseOrderMapper.updateById(order);
        return order;
    }

    /**
     * 采购收货入库
     *
     * @param orderId     采购订单ID
     * @param warehouseId 入库仓库ID
     * @param items       收货明细
     * @param operator    操作人
     * @return 更新后的订单
     */
    @Transactional
    public PurchaseOrder receiveGoods(Long orderId, Long warehouseId,
                                       List<ReceiveRequest.ReceiveItem> items, String operator) {
        log.info("采购收货入库: orderId={}, warehouseId={}, items={}", orderId, warehouseId, items);

        // 1. 校验订单
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"APPROVED".equals(order.getStatus()) && !"RECEIVING".equals(order.getStatus())) {
            throw new BusinessException("只有已审批或收货中的订单可以收货入库");
        }

        // 2. 校验仓库
        Warehouse warehouse = warehouseService.getById(warehouseId);
        if (warehouse == null || !"ACTIVE".equals(warehouse.getStatus())) {
            throw new BusinessException("仓库不存在或已停用");
        }

        // 3. 获取订单明细并建立映射
        List<PurchaseOrderItem> orderItems = purchaseOrderItemMapper.findByOrderId(orderId);
        Map<Long, PurchaseOrderItem> itemMap = orderItems.stream()
                .collect(Collectors.toMap(PurchaseOrderItem::getId, Function.identity()));

        // 4. 遍历收货明细，执行入库
        boolean allCompleted = true;
        for (ReceiveRequest.ReceiveItem receiveItem : items) {
            PurchaseOrderItem orderItem = itemMap.get(receiveItem.getItemId());
            if (orderItem == null) {
                throw new BusinessException("订单明细不存在: " + receiveItem.getItemId());
            }

            // 校验收货数量不能超过订单数量
            BigDecimal newReceivedQty = (orderItem.getReceivedQty() != null ? orderItem.getReceivedQty() : BigDecimal.ZERO)
                    .add(receiveItem.getQuantity());
            if (newReceivedQty.compareTo(orderItem.getQuantity()) > 0) {
                throw new BusinessException("收货数量超过订单数量: " + orderItem.getProductName());
            }

            // 执行入库
            inventoryService.stockIn(
                    warehouseId,
                    orderItem.getProductId(),
                    orderItem.getProductSku(),
                    orderItem.getProductName(),
                    receiveItem.getQuantity(),
                    orderItem.getUnitPrice(),
                    "PURCHASE_IN",
                    "PURCHASE_ORDER",
                    orderId,
                    order.getOrderNo(),
                    operator
            );

            // 更新明细已收货数量
            orderItem.setReceivedQty(newReceivedQty);
            if (newReceivedQty.compareTo(orderItem.getQuantity()) == 0) {
                orderItem.setStatus("RECEIVED");
            } else {
                orderItem.setStatus("PARTIAL");
                allCompleted = false;
            }
            purchaseOrderItemMapper.updateById(orderItem);
        }

        // 5. 更新订单状态
        if (allCompleted) {
            order.setStatus("COMPLETED");
            order.setActualDeliveryDate(LocalDate.now());
        } else {
            order.setStatus("RECEIVING");
        }
        purchaseOrderMapper.updateById(order);

        log.info("采购收货入库完成: orderId={}, status={}", orderId, order.getStatus());
        return order;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderMapper.selectCount(null);
        return "PO" + dateStr + String.format("%04d", count + 1);
    }
}
