package com.wangziyang.mes.system.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.system.entity.EquipmentGroup;

/**
 * <p>
 * 设备编组分页请求
 * </p>
 */
public class EquipmentGroupPageReq extends Page<EquipmentGroup> {

    private static final long serialVersionUID = 1L;

    private String name;
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
