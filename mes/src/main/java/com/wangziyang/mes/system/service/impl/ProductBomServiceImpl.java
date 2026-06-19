package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProductBom;
import com.wangziyang.mes.system.mapper.ProductBomMapper;
import com.wangziyang.mes.system.service.IProductBomService;
import org.springframework.stereotype.Service;

/**
 * 产品BOM服务实现
 */
@Service
public class ProductBomServiceImpl extends ServiceImpl<ProductBomMapper, ProductBom> implements IProductBomService {
}
