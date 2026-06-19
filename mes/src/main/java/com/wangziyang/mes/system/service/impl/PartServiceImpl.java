package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.Part;
import com.wangziyang.mes.system.mapper.PartMapper;
import com.wangziyang.mes.system.service.IPartService;
import org.springframework.stereotype.Service;

/**
 * 零部件服务实现
 */
@Service
public class PartServiceImpl extends ServiceImpl<PartMapper, Part> implements IPartService {
}
