package com.aierp.modules.purchase.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aierp.modules.purchase.entity.Product;
import com.aierp.modules.purchase.mapper.ProductMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {
}
