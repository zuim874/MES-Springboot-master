package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.MesProcess;
import com.wangziyang.mes.system.mapper.ProcessMapper;
import com.wangziyang.mes.system.service.IProcessService;
import org.springframework.stereotype.Service;

/**
 * 工序信息 服务实现
 */
@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, MesProcess> implements IProcessService {
}