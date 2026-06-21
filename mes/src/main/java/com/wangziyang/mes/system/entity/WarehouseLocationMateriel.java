package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 库位物料关联实体
 */
@TableName("sp_warehouse_location_materiel")
public class WarehouseLocationMateriel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 库位ID
     */
    private String locationId;

    /**
     * 物料ID
     */
    private String materielId;

    /**
     * 存放数量
     */
    private Integer quantity;

    /**
     * 逻辑删除
     */
    private String isDeleted;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getMaterielId() {
        return materielId;
    }

    public void setMaterielId(String materielId) {
        this.materielId = materielId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
