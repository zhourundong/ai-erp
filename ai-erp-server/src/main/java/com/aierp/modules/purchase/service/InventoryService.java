package com.aierp.modules.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aierp.modules.purchase.entity.Inventory;
import com.aierp.modules.purchase.mapper.InventoryMapper;
import org.springframework.stereotype.Service;

@Service
public class InventoryService extends ServiceImpl<InventoryMapper, Inventory> {
}
