package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.WorkTeamUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 班组员工关联 Mapper 接口
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
public interface WorkTeamUserMapper extends BaseMapper<WorkTeamUser> {

    @Select("SELECT user_id FROM sp_work_team_user WHERE team_id = #{teamId} AND is_deleted = '0'")
    List<String> selectUserIdsByTeamId(@Param("teamId") String teamId);
}
