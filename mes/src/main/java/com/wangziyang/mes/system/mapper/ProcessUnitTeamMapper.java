package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.ProcessUnitTeam;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 加工单元班组关联 Mapper 接口
 * </p>
 */
public interface ProcessUnitTeamMapper extends BaseMapper<ProcessUnitTeam> {

    @Select("SELECT team_id FROM sp_process_unit_team WHERE process_unit_id = #{processUnitId} AND is_deleted = '0'")
    List<String> selectTeamIdsByProcessUnitId(@Param("processUnitId") String processUnitId);
}
