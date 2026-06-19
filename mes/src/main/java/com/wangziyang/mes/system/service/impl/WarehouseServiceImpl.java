package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.Warehouse;
import com.wangziyang.mes.system.mapper.WarehouseMapper;
import com.wangziyang.mes.system.service.IWarehouseService;
import org.springframework.stereotype.Service;

/**
 * 库房服务实现
 */
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements IWarehouseService {
}
