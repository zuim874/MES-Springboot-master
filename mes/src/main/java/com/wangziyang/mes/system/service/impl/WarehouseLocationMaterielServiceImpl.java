package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import com.wangziyang.mes.system.mapper.WarehouseLocationMaterielMapper;
import com.wangziyang.mes.system.service.IWarehouseLocationMaterielService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 库位物料关联 服务实现类
 */
@Service
public class WarehouseLocationMaterielServiceImpl extends ServiceImpl<WarehouseLocationMaterielMapper, WarehouseLocationMateriel> implements IWarehouseLocationMaterielService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindMateriels(String locationId, List<WarehouseLocationMateriel> materiels) {
        // 查询当前有效的绑定关系
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        List<WarehouseLocationMateriel> existingList = baseMapper.selectList(wrapper);
        Map<String, WarehouseLocationMateriel> existingMap = new HashMap<>();
        for (WarehouseLocationMateriel e : existingList) {
            existingMap.put(e.getMaterielId(), e);
        }

        Set<String> newMaterielIds = new HashSet<>();
        if (materiels != null) {
            for (WarehouseLocationMateriel item : materiels) {
                if (item.getMaterielId() == null || item.getMaterielId().trim().isEmpty()) {
                    continue;
                }
                String mid = item.getMaterielId().trim();
                newMaterielIds.add(mid);

                if (existingMap.containsKey(mid)) {
                    // 已存在，更新数量
                    WarehouseLocationMateriel existing = existingMap.get(mid);
                    existing.setQuantity(item.getQuantity() != null && item.getQuantity() >= 1 ? item.getQuantity() : 1);
                    baseMapper.updateById(existing);
                } else {
                    // 不存在，插入新记录
                    item.setId(UUID.randomUUID().toString().replace("-", ""));
                    item.setLocationId(locationId);
                    if (item.getQuantity() == null || item.getQuantity() < 1) {
                        item.setQuantity(1);
                    }
                    baseMapper.insert(item);
                }
            }
        }

        // 逻辑删除旧列表中有但新列表中没有的
        for (WarehouseLocationMateriel existing : existingList) {
            if (!newMaterielIds.contains(existing.getMaterielId())) {
                baseMapper.deleteById(existing.getId());
            }
        }
    }

    @Override
    public List<WarehouseLocationMateriel> listByLocationId(String locationId) {
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        return baseMapper.selectList(wrapper);
    }
}
