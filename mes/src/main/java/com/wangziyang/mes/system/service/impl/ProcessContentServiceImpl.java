package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProcessContent;
import com.wangziyang.mes.system.mapper.ProcessContentMapper;
import com.wangziyang.mes.system.service.IProcessContentService;
import org.springframework.stereotype.Service;

/**
 * 工艺内容编制 服务实现
 */
@Service
public class ProcessContentServiceImpl extends ServiceImpl<ProcessContentMapper, ProcessContent> implements IProcessContentService {
}
