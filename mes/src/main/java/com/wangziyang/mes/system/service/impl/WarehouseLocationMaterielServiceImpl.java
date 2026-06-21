package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import com.wangziyang.mes.system.mapper.WarehouseLocationMaterielMapper;
import com.wangziyang.mes.system.service.IWarehouseLocationMaterielService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 库位物料关联 服务实现类
 */
@Service
public class WarehouseLocationMaterielServiceImpl extends ServiceImpl<WarehouseLocationMaterielMapper, WarehouseLocationMateriel> implements IWarehouseLocationMaterielService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindMateriels(String locationId, List<WarehouseLocationMateriel> materiels) {
        // 物理删除旧关系
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        baseMapper.delete(wrapper);

        // 保存新关系
        if (materiels != null && !materiels.isEmpty()) {
            for (WarehouseLocationMateriel item : materiels) {
                if (item.getMaterielId() == null || item.getMaterielId().trim().isEmpty()) {
                    continue;
                }
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                item.setLocationId(locationId);
                if (item.getQuantity() == null || item.getQuantity() < 1) {
                    item.setQuantity(1);
                }
                item.setIsDeleted("0");
                baseMapper.insert(item);
            }
        }
    }

    @Override
    public List<WarehouseLocationMateriel> listByLocationId(String locationId) {
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        wrapper.eq("is_deleted", "0");
        return baseMapper.selectList(wrapper);
    }
}
