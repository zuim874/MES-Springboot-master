package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 库房实体
 */
@TableName("sp_warehouse")
public class Warehouse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 库房编码
     */
    private String code;

    /**
     * 库房名称
     */
    private String name;

    /**
     * 库房类型（零件库/产品库）
     */
    private String type;

    /**
     * 组数
     */
    private Integer groupCount;

    /**
     * 排数
     */
    private Integer rowCount;

    /**
     * 层数
     */
    private Integer layerCount;

    /**
     * 列数
     */
    private Integer columnCount;

    /**
     * 默认库位长度(cm)
     */
    private Integer defaultLength;

    /**
     * 默认库位宽度(cm)
     */
    private Integer defaultWidth;

    /**
     * 默认库位高度(cm)
     */
    private Integer defaultHeight;

    /**
     * 逻辑删除
     */
    private String isDeleted;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(Integer groupCount) {
        this.groupCount = groupCount;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(Integer layerCount) {
        this.layerCount = layerCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public Integer getDefaultLength() {
        return defaultLength;
    }

    public void setDefaultLength(Integer defaultLength) {
        this.defaultLength = defaultLength;
    }

    public Integer getDefaultWidth() {
        return defaultWidth;
    }

    public void setDefaultWidth(Integer defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    public Integer getDefaultHeight() {
        return defaultHeight;
    }

    public void setDefaultHeight(Integer defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
