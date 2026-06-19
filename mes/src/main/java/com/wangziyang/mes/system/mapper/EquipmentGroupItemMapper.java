package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.EquipmentGroupItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 设备编组关联 Mapper 接口
 * </p>
 */
public interface EquipmentGroupItemMapper extends BaseMapper<EquipmentGroupItem> {

    @Select("SELECT equipment_id FROM sp_equipment_group_item WHERE group_id = #{groupId} AND is_deleted = '0'")
    List<String> selectEquipmentIdsByGroupId(@Param("groupId") String groupId);
}
