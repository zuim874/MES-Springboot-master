package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProcessUnitEquipmentGroup;
import com.wangziyang.mes.system.mapper.ProcessUnitEquipmentGroupMapper;
import com.wangziyang.mes.system.service.IProcessUnitEquipmentGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <p>
 * 加工单元设备编组关联 服务实现类
 * </p>
 */
@Service
public class ProcessUnitEquipmentGroupServiceImpl extends ServiceImpl<ProcessUnitEquipmentGroupMapper, ProcessUnitEquipmentGroup> implements IProcessUnitEquipmentGroupService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindEquipmentGroups(String processUnitId, List<String> equipmentGroupIds) {
        // 查询当前已有的绑定关系
        QueryWrapper<ProcessUnitEquipmentGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("process_unit_id", processUnitId);
        List<ProcessUnitEquipmentGroup> existingList = baseMapper.selectList(wrapper);
        Map<String, ProcessUnitEquipmentGroup> existingMap = new HashMap<>();
        for (ProcessUnitEquipmentGroup e : existingList) {
            existingMap.put(e.getEquipmentGroupId(), e);
        }

        Set<String> newGroupIds = new HashSet<>();
        if (equipmentGroupIds != null) {
            for (String groupId : equipmentGroupIds) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    continue;
                }
                String gid = groupId.trim();
                newGroupIds.add(gid);

                if (existingMap.containsKey(gid)) {
                    // 已存在，确保状态为未删除
                    ProcessUnitEquipmentGroup existing = existingMap.get(gid);
                    if (!"0".equals(existing.getIsDeleted())) {
                        existing.setIsDeleted("0");
                        baseMapper.updateById(existing);
                    }
                } else {
                    // 不存在，插入新记录
                    ProcessUnitEquipmentGroup item = new ProcessUnitEquipmentGroup();
                    item.setId(UUID.randomUUID().toString().replace("-", ""));
                    item.setProcessUnitId(processUnitId);
                    item.setEquipmentGroupId(gid);
                    item.setIsDeleted("0");
                    baseMapper.insert(item);
                }
            }
        }

        // 逻辑删除旧列表中有但新列表中没有的
        for (ProcessUnitEquipmentGroup existing : existingList) {
            if (!newGroupIds.contains(existing.getEquipmentGroupId())) {
                existing.setIsDeleted("1");
                baseMapper.updateById(existing);
            }
        }
    }
}
