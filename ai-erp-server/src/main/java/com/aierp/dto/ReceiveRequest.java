package com.aierp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购收货请求
 */
@Data
public class ReceiveRequest {

    /** 入库仓库ID */
    private Long warehouseId;

    /** 收货明细 */
    private List<ReceiveItem> items;

    /** 操作人 */
    private String operator;

    @Data
    public static class ReceiveItem {
        /** 订单明细ID */
        private Long itemId;

        /** 收货数量 */
        private BigDecimal quantity;
    }
}
