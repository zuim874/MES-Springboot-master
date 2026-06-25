package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库位物料关联 Mapper 接口
 */
public interface WarehouseLocationMaterielMapper extends BaseMapper<WarehouseLocationMateriel> {

    @Select("SELECT * FROM sp_warehouse_location_materiel WHERE location_id = #{locationId} AND is_deleted = '0'")
    List<WarehouseLocationMateriel> selectByLocationId(@Param("locationId") String locationId);

    @Delete("DELETE FROM sp_warehouse_location_materiel WHERE id = #{id}")
    int physicalDeleteById(@Param("id") String id);

    @Delete("DELETE FROM sp_warehouse_location_materiel WHERE location_id = #{locationId} AND materiel_id = #{materielId}")
    int physicalDeleteByLocationIdAndMaterielId(@Param("locationId") String locationId, @Param("materielId") String materielId);

    @Delete("DELETE FROM sp_warehouse_location_materiel WHERE materiel_id = #{materielId}")
    int physicalDeleteByMaterielId(@Param("materielId") String materielId);
}
