package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProductBomNode;
import com.wangziyang.mes.system.mapper.ProductBomNodeMapper;
import com.wangziyang.mes.system.service.IProductBomNodeService;
import org.springframework.stereotype.Service;

/**
 * 产品BOM节点表服务实现
 */
@Service
public class ProductBomNodeServiceImpl extends ServiceImpl<ProductBomNodeMapper, ProductBomNode> implements IProductBomNodeService {
}
