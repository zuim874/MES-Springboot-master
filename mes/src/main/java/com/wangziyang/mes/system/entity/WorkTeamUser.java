package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * <p>
 * 班组员工关联
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
@TableName("sp_work_team_user")
public class WorkTeamUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String teamId;

    private String userId;

    /**
     * 逻辑删除：1 表示删除，0 表示未删除，2 表示禁用
     */
    private String isDeleted;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
