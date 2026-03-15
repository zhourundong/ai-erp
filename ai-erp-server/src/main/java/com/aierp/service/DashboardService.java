package com.aierp.service;

import com.aierp.dto.DashboardStats;
import com.aierp.entity.*;
import com.aierp.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 驾驶舱服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final ProductMapper productMapper;
    private final SupplierMapper supplierMapper;
    private final CustomerMapper customerMapper;
    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;

    /**
     * 获取驾驶舱统计数据
     */
    public DashboardStats getStats() {
        return DashboardStats.builder()
                .purchaseOrderCount(getPurchaseOrderCount())
                .salesOrderCount(getSalesOrderCount())
                .productCount(getProductCount())
                .supplierCount(getSupplierCount())
                .customerCount(getCustomerCount())
                .warehouseCount(getWarehouseCount())
                .totalPurchaseAmount(getTotalPurchaseAmount())
                .totalSalesAmount(getTotalSalesAmount())
                .purchaseOrderStatus(getPurchaseOrderStatus())
                .salesOrderStatus(getSalesOrderStatus())
                .inventoryWarningCount(getInventoryWarningCount())
                .inventoryWarnings(getInventoryWarnings())
                .purchaseTrend(getPurchaseTrend())
                .salesTrend(getSalesTrend())
                .pendingApprovalCount(getPendingApprovalCount())
                .pendingReceiveCount(getPendingReceiveCount())
                .pendingShipCount(getPendingShipCount())
                .build();
    }

    private Long getPurchaseOrderCount() {
        return purchaseOrderMapper.selectCount(new LambdaQueryWrapper<>());
    }

    private Long getSalesOrderCount() {
        return salesOrderMapper.selectCount(new LambdaQueryWrapper<>());
    }

    private Long getProductCount() {
        return productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "ACTIVE")
        );
    }

    private Long getSupplierCount() {
        return supplierMapper.selectCount(
                new LambdaQueryWrapper<Supplier>().eq(Supplier::getStatus, "ACTIVE")
        );
    }

    private Long getCustomerCount() {
        return customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>().eq(Customer::getStatus, "ACTIVE")
        );
    }

    private Long getWarehouseCount() {
        return warehouseMapper.selectCount(
                new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, "ACTIVE")
        );
    }

    private BigDecimal getTotalPurchaseAmount() {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .select(PurchaseOrder::getTotalAmount)
                        .isNotNull(PurchaseOrder::getTotalAmount)
        );
        return orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalSalesAmount() {
        List<SalesOrder> orders = salesOrderMapper.selectList(
                new LambdaQueryWrapper<SalesOrder>()
                        .select(SalesOrder::getTotalAmount)
                        .isNotNull(SalesOrder::getTotalAmount)
        );
        return orders.stream()
                .map(SalesOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DashboardStats.StatusCount> getPurchaseOrderStatus() {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>().select(PurchaseOrder::getStatus)
        );

        Map<String, Long> countMap = orders.stream()
                .collect(Collectors.groupingBy(PurchaseOrder::getStatus, Collectors.counting()));

        Map<String, String> labelMap = Map.of(
                "DRAFT", "草稿",
                "PENDING", "待审批",
                "APPROVED", "已审批",
                "RECEIVING", "收货中",
                "COMPLETED", "已完成",
                "REJECTED", "已拒绝"
        );

        return countMap.entrySet().stream()
                .map(e -> DashboardStats.StatusCount.builder()
                        .status(e.getKey())
                        .label(labelMap.getOrDefault(e.getKey(), e.getKey()))
                        .count(e.getValue())
                        .build())
                .sorted(Comparator.comparing(DashboardStats.StatusCount::getCount, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private List<DashboardStats.StatusCount> getSalesOrderStatus() {
        List<SalesOrder> orders = salesOrderMapper.selectList(
                new LambdaQueryWrapper<SalesOrder>().select(SalesOrder::getStatus)
        );

        Map<String, Long> countMap = orders.stream()
                .collect(Collectors.groupingBy(SalesOrder::getStatus, Collectors.counting()));

        Map<String, String> labelMap = Map.of(
                "DRAFT", "草稿",
                "CONFIRMED", "已确认",
                "SHIPPING", "发货中",
                "COMPLETED", "已完成",
                "CANCELLED", "已取消"
        );

        return countMap.entrySet().stream()
                .map(e -> DashboardStats.StatusCount.builder()
                        .status(e.getKey())
                        .label(labelMap.getOrDefault(e.getKey(), e.getKey()))
                        .count(e.getValue())
                        .build())
                .sorted(Comparator.comparing(DashboardStats.StatusCount::getCount, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private Long getInventoryWarningCount() {
        return (long) getInventoryWarnings().size();
    }

    private List<DashboardStats.InventoryWarning> getInventoryWarnings() {
        // 获取所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<>());

        // 按商品分组，计算总库存
        Map<Long, BigDecimal> stockMap = inventories.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getProductId,
                        Collectors.reducing(BigDecimal.ZERO, i -> i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO, BigDecimal::add)
                ));

        // 获取商品信息
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "ACTIVE")
        );

        List<DashboardStats.InventoryWarning> warnings = new ArrayList<>();

        for (Product product : products) {
            BigDecimal currentStock = stockMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            Integer safetyStock = product.getSafetyStock() != null ? product.getSafetyStock() : 0;

            if (currentStock.compareTo(BigDecimal.valueOf(safetyStock)) < 0) {
                // 找到库存所在的仓库名称
                String warehouseName = inventories.stream()
                        .filter(i -> i.getProductId().equals(product.getId()) && i.getQuantity() != null && i.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                        .findFirst()
                        .map(i -> {
                            Warehouse wh = warehouseMapper.selectById(i.getWarehouseId());
                            return wh != null ? wh.getName() : "";
                        })
                        .orElse("");

                warnings.add(DashboardStats.InventoryWarning.builder()
                        .productId(product.getId())
                        .productSku(product.getSku())
                        .productName(product.getName())
                        .safetyStock(safetyStock)
                        .currentStock(currentStock.intValue())
                        .warehouseName(warehouseName)
                        .build());
            }
        }

        return warnings.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<DashboardStats.DailyTrend> getPurchaseTrend() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        List<DashboardStats.DailyTrend> trend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrder>()
                            .eq(PurchaseOrder::getOrderDate, date)
            );

            BigDecimal amount = orders.stream()
                    .map(PurchaseOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trend.add(DashboardStats.DailyTrend.builder()
                    .date(date.format(formatter))
                    .count((long) orders.size())
                    .amount(amount)
                    .build());
        }

        return trend;
    }

    private List<DashboardStats.DailyTrend> getSalesTrend() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        List<DashboardStats.DailyTrend> trend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<SalesOrder> orders = salesOrderMapper.selectList(
                    new LambdaQueryWrapper<SalesOrder>()
                            .eq(SalesOrder::getOrderDate, date)
            );

            BigDecimal amount = orders.stream()
                    .map(SalesOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trend.add(DashboardStats.DailyTrend.builder()
                    .date(date.format(formatter))
                    .count((long) orders.size())
                    .amount(amount)
                    .build());
        }

        return trend;
    }

    private Long getPendingApprovalCount() {
        return purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getStatus, "PENDING")
        );
    }

    private Long getPendingReceiveCount() {
        return purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getStatus, "APPROVED")
        );
    }

    private Long getPendingShipCount() {
        return salesOrderMapper.selectCount(
                new LambdaQueryWrapper<SalesOrder>().eq(SalesOrder::getStatus, "CONFIRMED")
        );
    }
}
