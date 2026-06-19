package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * <p>
 * 加工单元
 * </p>
 */
@TableName("sp_process_unit")
public class ProcessUnit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    private String type;

    private String hasLineSideWarehouse;

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

    public String getHasLineSideWarehouse() {
        return hasLineSideWarehouse;
    }

    public void setHasLineSideWarehouse(String hasLineSideWarehouse) {
        this.hasLineSideWarehouse = hasLineSideWarehouse;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
