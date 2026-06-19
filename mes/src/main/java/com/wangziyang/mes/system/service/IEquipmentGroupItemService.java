package com.wangziyang.mes.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangziyang.mes.system.entity.EquipmentGroupItem;

import java.util.List;

/**
 * <p>
 * 设备编组关联 服务类
 * </p>
 */
public interface IEquipmentGroupItemService extends IService<EquipmentGroupItem> {

    void saveBindEquipments(String groupId, List<String> equipmentIds);
}
