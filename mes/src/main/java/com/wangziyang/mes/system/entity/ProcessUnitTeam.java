package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * <p>
 * 加工单元班组关联
 * </p>
 */
@TableName("sp_process_unit_team")
public class ProcessUnitTeam extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String processUnitId;

    private String teamId;

    private String isDeleted;

    public String getProcessUnitId() {
        return processUnitId;
    }

    public void setProcessUnitId(String processUnitId) {
        this.processUnitId = processUnitId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
