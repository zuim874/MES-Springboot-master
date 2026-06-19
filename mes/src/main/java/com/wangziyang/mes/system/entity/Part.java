package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 零部件实体
 */
@TableName("sp_part")
public class Part extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 零部件编号
     */
    private String code;

    /**
     * 零部件名称
     */
    private String name;

    /**
     * 备注描述
     */
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
