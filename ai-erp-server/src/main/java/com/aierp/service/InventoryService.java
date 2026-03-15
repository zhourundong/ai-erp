package com.aierp.service;

import com.aierp.entity.Inventory;
import com.aierp.entity.InventoryTransaction;
import com.aierp.entity.Product;
import com.aierp.entity.Warehouse;
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
    private final WarehouseService warehouseService;
    private final ProductService productService;

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

    /**
     * 分页查询库存流水
     */
    public Page<InventoryTransaction> pageTransactions(int pageNum, int pageSize, Long warehouseId, Long productId, String transactionType) {
        Page<InventoryTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryTransaction> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(InventoryTransaction::getWarehouseId, warehouseId);
        }
        if (productId != null) {
            wrapper.eq(InventoryTransaction::getProductId, productId);
        }
        if (transactionType != null && !transactionType.isEmpty()) {
            wrapper.eq(InventoryTransaction::getTransactionType, transactionType);
        }
        wrapper.orderByDesc(InventoryTransaction::getCreatedAt);
        return transactionMapper.selectPage(page, wrapper);
    }

    public Inventory getByWarehouseAndProduct(Long warehouseId, Long productId) {
        return inventoryMapper.findByWarehouseAndProduct(warehouseId, productId);
    }

    /**
     * 入库
     * @param transactionType 交易类型，默认 OTHER_IN
     */
    @Transactional
    public Inventory stockIn(Long warehouseId, Long productId, String productSku, String productName,
                              BigDecimal quantity, BigDecimal costPrice, String transactionType,
                              String orderType, Long orderId, String orderNo, String operator) {

        // 如果 productId 为 null，尝试通过 SKU 查找商品
        if (productId == null && productSku != null && !productSku.isEmpty()) {
            Product product = productService.getBySku(productSku);
            if (product != null) {
                productId = product.getId();
            }
        }

        // 如果仍然没有 productId，生成一个临时ID（负数表示临时）
        if (productId == null) {
            // 使用 SKU 的 hashCode 生成一个临时 productId（正数）
            productId = (long) Math.abs(productSku != null ? productSku.hashCode() : productName.hashCode());
            // 确保不与真实商品ID冲突（使用大数值区间）
            if (productId < 1000000) {
                productId += 1000000;
            }
        }

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

        // 记录流水，使用传入的交易类型，默认为 OTHER_IN
        String type = (transactionType != null && !transactionType.isEmpty()) ? transactionType : "OTHER_IN";
        createTransaction(type, warehouseId, productId, productSku, productName,
                quantity, beforeQty, inventory.getQuantity(), costPrice, orderType, orderId, orderNo, operator);

        return inventory;
    }

    /**
     * 出库
     * @param transactionType 交易类型，默认 OTHER_OUT
     * @param unitPrice 交易单价（销售出库时为销售单价，其他出库时为成本价）
     */
    @Transactional
    public Inventory stockOut(Long warehouseId, Long productId, BigDecimal quantity, String transactionType,
                               BigDecimal unitPrice, String orderType, Long orderId, String orderNo, String operator) {
        Inventory inventory = inventoryMapper.findByWarehouseAndProduct(warehouseId, productId);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        if (inventory.getAvailableQty().compareTo(quantity) < 0) {
            throw new RuntimeException("库存不足");
        }

        BigDecimal beforeQty = inventory.getQuantity();
        // 使用传入的单价，如果未传入则使用库存成本价
        BigDecimal price = unitPrice != null ? unitPrice :
                (inventory.getCostPrice() != null ? inventory.getCostPrice() : BigDecimal.ZERO);

        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
        inventory.setAvailableQty(inventory.getAvailableQty().subtract(quantity));
        inventory.setLastOutDate(LocalDateTime.now());
        inventoryMapper.updateById(inventory);

        // 记录流水，使用传入的交易类型，默认为 OTHER_OUT
        String type = (transactionType != null && !transactionType.isEmpty()) ? transactionType : "OTHER_OUT";
        createTransaction(type, warehouseId, productId, inventory.getProductSku(), inventory.getProductName(),
                quantity.negate(), beforeQty, inventory.getQuantity(), price,
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

        // 查询并设置仓库名称
        Warehouse warehouse = warehouseService.getById(warehouseId);
        if (warehouse != null) {
            transaction.setWarehouseName(warehouse.getName());
        }

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
