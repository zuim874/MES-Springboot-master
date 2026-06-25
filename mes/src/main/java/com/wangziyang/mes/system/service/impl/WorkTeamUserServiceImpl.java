package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.WorkTeamUser;
import com.wangziyang.mes.system.mapper.WorkTeamUserMapper;
import com.wangziyang.mes.system.service.IWorkTeamUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 班组员工关联 服务实现类
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
@Service
public class WorkTeamUserServiceImpl extends ServiceImpl<WorkTeamUserMapper, WorkTeamUser> implements IWorkTeamUserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindUsers(String teamId, List<String> userIds) {
        // 先删除该班组已有的绑定关系
        QueryWrapper<WorkTeamUser> wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        baseMapper.delete(wrapper);

        // 插入新的绑定关系
        if (userIds != null && !userIds.isEmpty()) {
            // 约束：一个用户只能绑定到一个班组，先清除这些用户在所有其他班组的绑定
            for (String userId : userIds) {
                QueryWrapper<WorkTeamUser> userWrapper = new QueryWrapper<>();
                userWrapper.eq("user_id", userId);
                baseMapper.delete(userWrapper);
            }

            List<WorkTeamUser> list = new ArrayList<>();
            for (String userId : userIds) {
                WorkTeamUser tu = new WorkTeamUser();
                tu.setId(UUID.randomUUID().toString().replace("-", ""));
                tu.setTeamId(teamId);
                tu.setUserId(userId);
                tu.setIsDeleted("0");
                list.add(tu);
            }
            if (!list.isEmpty()) {
                for (WorkTeamUser tu : list) {
                    baseMapper.insert(tu);
                }
            }
        }
    }
}
