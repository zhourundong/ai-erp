package com.aierp.service;

import com.aierp.entity.Inventory;
import com.aierp.entity.InventoryTransaction;
import com.aierp.mapper.InventoryMapper;
import com.aierp.mapper.InventoryTransactionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;

    public Page<Inventory> pageInventory(int pageNum, int pageSize, Long warehouseId, Long productId) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }
        if (productId != null) {
            wrapper.eq(Inventory::getProductId, productId);
        }
        wrapper.orderByDesc(Inventory::getUpdatedAt);
        return inventoryMapper.selectPage(page, wrapper);
    }

    public Inventory getByWarehouseAndProduct(Long warehouseId, Long productId) {
        return inventoryMapper.findByWarehouseAndProduct(warehouseId, productId);
    }

    /**
     * 入库
     */
    @Transactional
    public Inventory stockIn(Long warehouseId, Long productId, String productSku, String productName,
                              BigDecimal quantity, BigDecimal costPrice, String orderType, Long orderId, String orderNo, String operator) {
        Inventory inventory = inventoryMapper.findByWarehouseAndProduct(warehouseId, productId);

        BigDecimal beforeQty = BigDecimal.ZERO;
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setWarehouseId(warehouseId);
            inventory.setProductId(productId);
            inventory.setProductSku(productSku);
            inventory.setProductName(productName);
            inventory.setQuantity(quantity);
            inventory.setAvailableQty(quantity);
            inventory.setLockedQty(BigDecimal.ZERO);
            inventory.setCostPrice(costPrice);
            inventory.setLastInDate(LocalDateTime.now());
            inventoryMapper.insert(inventory);
            beforeQty = BigDecimal.ZERO;
        } else {
            beforeQty = inventory.getQuantity();
            inventory.setQuantity(inventory.getQuantity().add(quantity));
            inventory.setAvailableQty(inventory.getAvailableQty().add(quantity));
            inventory.setCostPrice(costPrice);
            inventory.setLastInDate(LocalDateTime.now());
            inventoryMapper.updateById(inventory);
        }

        // 记录流水
        createTransaction("PURCHASE_IN", warehouseId, productId, productSku, productName,
                quantity, beforeQty, inventory.getQuantity(), costPrice, orderType, orderId, orderNo, operator);

        return inventory;
    }

    /**
     * 出库
     */
    @Transactional
    public Inventory stockOut(Long warehouseId, Long productId, BigDecimal quantity,
                               String orderType, Long orderId, String orderNo, String operator) {
        Inventory inventory = inventoryMapper.findByWarehouseAndProduct(warehouseId, productId);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        if (inventory.getAvailableQty().compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足");
        }

        BigDecimal beforeQty = inventory.getQuantity();
        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
        inventory.setAvailableQty(inventory.getAvailableQty().subtract(quantity));
        inventory.setLastOutDate(LocalDateTime.now());
        inventoryMapper.updateById(inventory);

        // 记录流水
        createTransaction("SALES_OUT", warehouseId, productId, inventory.getProductSku(), inventory.getProductName(),
                quantity.negate(), beforeQty, inventory.getQuantity(), inventory.getCostPrice(),
                orderType, orderId, orderNo, operator);

        return inventory;
    }

    private void createTransaction(String type, Long warehouseId, Long productId, String productSku, String productName,
                                   BigDecimal quantity, BigDecimal beforeQty, BigDecimal afterQty, BigDecimal unitCost,
                                   String orderType, Long orderId, String orderNo, String operator) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNo("TXN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        transaction.setTransactionType(type);
        transaction.setWarehouseId(warehouseId);
        transaction.setProductId(productId);
        transaction.setProductSku(productSku);
        transaction.setProductName(productName);
        transaction.setQuantity(quantity);
        transaction.setBeforeQty(beforeQty);
        transaction.setAfterQty(afterQty);
        transaction.setUnitCost(unitCost);
        transaction.setTotalCost(unitCost != null ? unitCost.multiply(quantity.abs()) : BigDecimal.ZERO);
        transaction.setRelatedOrderType(orderType);
        transaction.setRelatedOrderId(orderId);
        transaction.setRelatedOrderNo(orderNo);
        transaction.setOperator(operator);
        transactionMapper.insert(transaction);
    }
}
