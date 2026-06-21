package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 库位实体
 */
@TableName("sp_warehouse_location")
public class WarehouseLocation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 库房ID
     */
    private String warehouseId;

    /**
     * 库位编码
     */
    private String code;

    /**
     * 组号
     */
    private Integer groupNum;

    /**
     * 排号
     */
    private Integer rowNum;

    /**
     * 层号
     */
    private Integer layerNum;

    /**
     * 列号
     */
    private Integer columnNum;

    /**
     * 当前存放物料ID
     */
    private String materielId;

    /**
     * 库位长度(cm)
     */
    private Integer length;

    /**
     * 库位宽度(cm)
     */
    private Integer width;

    /**
     * 库位高度(cm)
     */
    private Integer height;

    /**
     * 逻辑删除
     */
    @TableLogic(value = "0", delval = "1")
    private String isDeleted;

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(Integer groupNum) {
        this.groupNum = groupNum;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getLayerNum() {
        return layerNum;
    }

    public void setLayerNum(Integer layerNum) {
        this.layerNum = layerNum;
    }

    public Integer getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(Integer columnNum) {
        this.columnNum = columnNum;
    }

    public String getMaterielId() {
        return materielId;
    }

    public void setMaterielId(String materielId) {
        this.materielId = materielId;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
