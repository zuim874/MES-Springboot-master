package com.wangziyang.mes.system.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.system.entity.Equipment;

/**
 * <p>
 * 生产设备分页请求
 * </p>
 */
public class EquipmentPageReq extends Page<Equipment> {

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
