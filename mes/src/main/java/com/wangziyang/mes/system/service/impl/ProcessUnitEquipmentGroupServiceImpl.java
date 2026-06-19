package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.ProcessUnitEquipmentGroup;
import com.wangziyang.mes.system.mapper.ProcessUnitEquipmentGroupMapper;
import com.wangziyang.mes.system.service.IProcessUnitEquipmentGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
        QueryWrapper<ProcessUnitEquipmentGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("process_unit_id", processUnitId);
        baseMapper.delete(wrapper);

        if (equipmentGroupIds != null && !equipmentGroupIds.isEmpty()) {
            List<ProcessUnitEquipmentGroup> list = new ArrayList<>();
            for (String equipmentGroupId : equipmentGroupIds) {
                ProcessUnitEquipmentGroup item = new ProcessUnitEquipmentGroup();
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                item.setProcessUnitId(processUnitId);
                item.setEquipmentGroupId(equipmentGroupId);
                item.setIsDeleted("0");
                list.add(item);
            }
            for (ProcessUnitEquipmentGroup item : list) {
                baseMapper.insert(item);
            }
        }
    }
}
