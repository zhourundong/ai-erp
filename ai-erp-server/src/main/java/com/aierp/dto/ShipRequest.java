package com.aierp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售发货请求
 */
@Data
public class ShipRequest {

    /** 出库仓库ID */
    private Long warehouseId;

    /** 发货明细 */
    private List<ShipItem> items;

    /** 操作人 */
    private String operator;

    @Data
    public static class ShipItem {
        /** 订单明细ID */
        private Long itemId;

        /** 发货数量 */
        private BigDecimal quantity;
    }
}
