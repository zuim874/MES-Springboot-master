package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.ProcessUnitEquipmentGroup;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 加工单元设备编组关联 Mapper 接口
 * </p>
 */
public interface ProcessUnitEquipmentGroupMapper extends BaseMapper<ProcessUnitEquipmentGroup> {

    @Select("SELECT equipment_group_id FROM sp_process_unit_equipment_group WHERE process_unit_id = #{processUnitId} AND is_deleted = '0'")
    List<String> selectEquipmentGroupIdsByProcessUnitId(@Param("processUnitId") String processUnitId);

    @Select("SELECT CONCAT(p.code, '(', p.name, ')') FROM sp_process_unit p INNER JOIN sp_process_unit_equipment_group t ON p.id = t.process_unit_id WHERE t.equipment_group_id = #{equipmentGroupId} AND t.is_deleted = '0' AND p.is_deleted = '0'")
    List<String> selectProcessUnitNamesByEquipmentGroupId(@Param("equipmentGroupId") String equipmentGroupId);
}
