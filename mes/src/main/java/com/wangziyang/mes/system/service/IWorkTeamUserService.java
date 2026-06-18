package com.wangziyang.mes.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangziyang.mes.system.entity.WorkTeamUser;

import java.util.List;

/**
 * <p>
 * 班组员工关联 服务类
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
public interface IWorkTeamUserService extends IService<WorkTeamUser> {

    /**
     * 保存班组员工绑定关系
     */
    void saveBindUsers(String teamId, List<String> userIds);
}
