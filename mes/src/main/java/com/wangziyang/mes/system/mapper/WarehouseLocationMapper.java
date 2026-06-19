package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.WarehouseLocation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库位 Mapper
 */
public interface WarehouseLocationMapper extends BaseMapper<WarehouseLocation> {

    @Select("SELECT * FROM sp_warehouse_location WHERE warehouse_id = #{warehouseId} AND is_deleted = '0' ORDER BY group_num, row_num, layer_num, column_num")
    List<WarehouseLocation> selectByWarehouseId(@Param("warehouseId") String warehouseId);
}
