package com.wangziyang.mes.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangziyang.mes.system.entity.ProcessUnitEquipmentGroup;

import java.util.List;

/**
 * <p>
 * 加工单元设备编组关联 服务类
 * </p>
 */
public interface IProcessUnitEquipmentGroupService extends IService<ProcessUnitEquipmentGroup> {

    void saveBindEquipmentGroups(String processUnitId, List<String> equipmentGroupIds);
}
