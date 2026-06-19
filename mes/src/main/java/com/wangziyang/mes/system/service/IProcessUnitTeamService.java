package com.wangziyang.mes.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangziyang.mes.system.entity.ProcessUnitTeam;

import java.util.List;

/**
 * <p>
 * 加工单元班组关联 服务类
 * </p>
 */
public interface IProcessUnitTeamService extends IService<ProcessUnitTeam> {

    void saveBindTeams(String processUnitId, List<String> teamIds);
}
