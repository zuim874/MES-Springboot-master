package com.wangziyang.mes.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;

import java.util.List;

/**
 * 库位物料关联 服务类
 */
public interface IWarehouseLocationMaterielService extends IService<WarehouseLocationMateriel> {

    /**
     * 保存库位物料绑定关系（先删除旧关系，再保存新关系）
     */
    void saveBindMateriels(String locationId, List<WarehouseLocationMateriel> materiels);

    /**
     * 根据库位ID查询物料列表
     */
    List<WarehouseLocationMateriel> listByLocationId(String locationId);
}
