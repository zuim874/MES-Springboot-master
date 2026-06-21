package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProcessFlow;
import com.wangziyang.mes.system.mapper.ProcessFlowMapper;
import com.wangziyang.mes.system.service.IProcessFlowService;
import org.springframework.stereotype.Service;

@Service
public class ProcessFlowServiceImpl extends ServiceImpl<ProcessFlowMapper, ProcessFlow> implements IProcessFlowService {
}
