package com.aierp.modules.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aierp.modules.purchase.entity.PurchaseOrder;
import com.aierp.modules.purchase.mapper.PurchaseOrderMapper;
import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {
}
