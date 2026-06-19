package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProcessUnitTeam;
import com.wangziyang.mes.system.mapper.ProcessUnitTeamMapper;
import com.wangziyang.mes.system.service.IProcessUnitTeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 加工单元班组关联 服务实现类
 * </p>
 */
@Service
public class ProcessUnitTeamServiceImpl extends ServiceImpl<ProcessUnitTeamMapper, ProcessUnitTeam> implements IProcessUnitTeamService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindTeams(String processUnitId, List<String> teamIds) {
        QueryWrapper<ProcessUnitTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("process_unit_id", processUnitId);
        baseMapper.delete(wrapper);

        if (teamIds != null && !teamIds.isEmpty()) {
            List<ProcessUnitTeam> list = new ArrayList<>();
            for (String teamId : teamIds) {
                ProcessUnitTeam item = new ProcessUnitTeam();
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                item.setProcessUnitId(processUnitId);
                item.setTeamId(teamId);
                item.setIsDeleted("0");
                list.add(item);
            }
            for (ProcessUnitTeam item : list) {
                baseMapper.insert(item);
            }
        }
    }
}
