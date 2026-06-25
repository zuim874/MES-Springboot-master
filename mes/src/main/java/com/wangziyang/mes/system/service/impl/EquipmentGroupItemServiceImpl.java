package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.EquipmentGroupItem;
import com.wangziyang.mes.system.mapper.EquipmentGroupItemMapper;
import com.wangziyang.mes.system.service.IEquipmentGroupItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 设备编组关联 服务实现类
 * </p>
 */
@Service
public class EquipmentGroupItemServiceImpl extends ServiceImpl<EquipmentGroupItemMapper, EquipmentGroupItem> implements IEquipmentGroupItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindEquipments(String groupId, List<String> equipmentIds) {
        QueryWrapper<EquipmentGroupItem> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        baseMapper.delete(wrapper);

        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            // 约束：一台设备只能绑定到一个编组，先清除这些设备在所有其他编组的绑定
            for (String equipmentId : equipmentIds) {
                QueryWrapper<EquipmentGroupItem> eqWrapper = new QueryWrapper<>();
                eqWrapper.eq("equipment_id", equipmentId);
                baseMapper.delete(eqWrapper);
            }

            List<EquipmentGroupItem> list = new ArrayList<>();
            for (String equipmentId : equipmentIds) {
                EquipmentGroupItem item = new EquipmentGroupItem();
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                item.setGroupId(groupId);
                item.setEquipmentId(equipmentId);
                item.setIsDeleted("0");
                list.add(item);
            }
            for (EquipmentGroupItem item : list) {
                baseMapper.insert(item);
            }
        }
    }
}
