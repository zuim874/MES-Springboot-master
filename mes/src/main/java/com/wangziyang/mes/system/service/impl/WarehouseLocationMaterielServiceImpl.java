package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.WarehouseLocation;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import com.wangziyang.mes.system.mapper.WarehouseLocationMaterielMapper;
import com.wangziyang.mes.system.service.IWarehouseLocationMaterielService;
import com.wangziyang.mes.system.service.IWarehouseLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 库位物料关联 服务实现类
 */
@Service
public class WarehouseLocationMaterielServiceImpl extends ServiceImpl<WarehouseLocationMaterielMapper, WarehouseLocationMateriel> implements IWarehouseLocationMaterielService {

    @Autowired
    private IWarehouseLocationService warehouseLocationService;

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
                Integer qty = item.getQuantity() != null ? item.getQuantity() : 1;

                // 数量小于等于0，删除该绑定关系（支持部分删除后清空库存）
                if (qty <= 0) {
                    if (existingMap.containsKey(mid)) {
                        baseMapper.deleteById(existingMap.get(mid).getId());
                    }
                    continue;
                }

                newMaterielIds.add(mid);

                if (existingMap.containsKey(mid)) {
                    // 已存在，更新数量
                    WarehouseLocationMateriel existing = existingMap.get(mid);
                    existing.setQuantity(qty);
                    baseMapper.updateById(existing);
                } else {
                    // 不存在，插入新记录
                    item.setId(UUID.randomUUID().toString().replace("-", ""));
                    item.setLocationId(locationId);
                    item.setQuantity(qty);
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

        // 同步更新 sp_warehouse_location 的 materiel_id
        syncLocationMaterielId(locationId);
    }

    /**
     * 同步库位主物料ID：根据当前有效绑定关系更新 materiel_id
     */
    private void syncLocationMaterielId(String locationId) {
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        wrapper.orderByAsc("create_time");
        List<WarehouseLocationMateriel> list = baseMapper.selectList(wrapper);
        UpdateWrapper<WarehouseLocation> locationWrapper = new UpdateWrapper<>();
        locationWrapper.eq("id", locationId);
        if (list.isEmpty()) {
            locationWrapper.set("materiel_id", null);
        } else {
            locationWrapper.set("materiel_id", list.get(0).getMaterielId());
        }
        warehouseLocationService.update(null, locationWrapper);
    }

    @Override
    public List<WarehouseLocationMateriel> listByLocationId(String locationId) {
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        return baseMapper.selectList(wrapper);
    }
}
