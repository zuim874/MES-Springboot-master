package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProductBomItem;
import com.wangziyang.mes.system.mapper.ProductBomItemMapper;
import com.wangziyang.mes.system.service.IProductBomItemService;
import org.springframework.stereotype.Service;

/**
 * 产品BOM子项服务实现
 */
@Service
public class ProductBomItemServiceImpl extends ServiceImpl<ProductBomItemMapper, ProductBomItem> implements IProductBomItemService {
}
