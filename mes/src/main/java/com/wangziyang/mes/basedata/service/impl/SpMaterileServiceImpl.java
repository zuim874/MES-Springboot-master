package com.wangziyang.mes.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wangziyang.mes.basedata.entity.SpMaterile;
import com.wangziyang.mes.basedata.mapper.SpMaterileMapper;
import com.wangziyang.mes.basedata.service.ISpMaterileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.system.entity.WarehouseLocation;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import com.wangziyang.mes.system.mapper.WarehouseLocationMapper;
import com.wangziyang.mes.system.mapper.WarehouseLocationMaterielMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

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
