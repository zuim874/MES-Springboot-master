package com.wangziyang.mes.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wangziyang.mes.basedata.entity.SpMaterile;
import com.wangziyang.mes.basedata.mapper.SpMaterileMapper;
import com.wangziyang.mes.basedata.service.ISpMaterileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.WarehouseLocation;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import com.wangziyang.mes.system.mapper.WarehouseLocationMapper;
import com.wangziyang.mes.system.mapper.WarehouseLocationMaterielMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangZiYang
 * @since 2020-03-19
 */
@Service
public class SpMaterileServiceImpl extends ServiceImpl<SpMaterileMapper, SpMaterile> implements ISpMaterileService {

    @Autowired
    private WarehouseLocationMapper warehouseLocationMapper;

    @Autowired
    private WarehouseLocationMaterielMapper warehouseLocationMaterielMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(SpMaterile entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result && StringUtils.isNotEmpty(entity.getLocationId()) && entity.getStock() != null) {
            syncDefaultLocationStock(entity);
        }
        return result;
    }

    /**
     * 同步默认库位库存：将物料的 stock 同步到默认库位的 quantity
     */
    private void syncDefaultLocationStock(SpMaterile entity) {
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", entity.getLocationId());
        wrapper.eq("materiel_id", entity.getId());
        WarehouseLocationMateriel exist = warehouseLocationMaterielMapper.selectOne(wrapper);

        if (exist != null) {
            if (entity.getStock() <= 0) {
                // 库存为0或负数，删除默认库位上的绑定关系
                warehouseLocationMaterielMapper.deleteById(exist.getId());
            } else {
                exist.setQuantity(entity.getStock());
                warehouseLocationMaterielMapper.updateById(exist);
            }
        } else if (entity.getStock() > 0) {
            // 默认库位上不存在该物料，且库存大于0，创建新记录
            WarehouseLocationMateriel newRecord = new WarehouseLocationMateriel();
            newRecord.setId(UUID.randomUUID().toString().replace("-", ""));
            newRecord.setLocationId(entity.getLocationId());
            newRecord.setMaterielId(entity.getId());
            newRecord.setQuantity(entity.getStock());
            warehouseLocationMaterielMapper.insert(newRecord);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 1. 清空库位上的当前存放物料ID（使用 wrapper.set 强制更新 null）
        UpdateWrapper<WarehouseLocation> locationWrapper = new UpdateWrapper<>();
        locationWrapper.eq("materiel_id", id).set("materiel_id", null);
        warehouseLocationMapper.update(null, locationWrapper);

        // 2. 逻辑删除库位物料关联记录
        WarehouseLocationMateriel materielUpdate = new WarehouseLocationMateriel();
        materielUpdate.setIsDeleted("1");
        UpdateWrapper<WarehouseLocationMateriel> materielWrapper = new UpdateWrapper<>();
        materielWrapper.eq("materiel_id", id);
        warehouseLocationMaterielMapper.update(materielUpdate, materielWrapper);

        // 3. 逻辑删除物料
        return super.removeById(id);
    }
}
