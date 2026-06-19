package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * <p>
 * 加工单元设备编组关联
 * </p>
 */
@TableName("sp_process_unit_equipment_group")
public class ProcessUnitEquipmentGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String processUnitId;

    private String equipmentGroupId;

    private String isDeleted;

    public String getProcessUnitId() {
        return processUnitId;
    }

    public void setProcessUnitId(String processUnitId) {
        this.processUnitId = processUnitId;
    }

    public String getEquipmentGroupId() {
        return equipmentGroupId;
    }

    public void setEquipmentGroupId(String equipmentGroupId) {
        this.equipmentGroupId = equipmentGroupId;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
