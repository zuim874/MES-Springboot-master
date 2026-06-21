package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProcessPlan;
import com.wangziyang.mes.system.mapper.ProcessPlanMapper;
import com.wangziyang.mes.system.service.IProcessPlanService;
import org.springframework.stereotype.Service;

/**
 * 工艺规划 服务实现
 */
@Service
public class ProcessPlanServiceImpl extends ServiceImpl<ProcessPlanMapper, ProcessPlan> implements IProcessPlanService {
}