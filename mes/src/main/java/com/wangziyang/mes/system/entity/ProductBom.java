package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 产品BOM头表实体
 */
@TableName("sp_product_bom")
public class ProductBom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 产品物料编码
     */
    private String code;

    /**
     * 产品物料名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 有效性 0:无效 1:有效
     */
    private String isValid;

    /**
     * 定版标识 0:未定版 1:已定版
     */
    private String isFrozen;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public String getIsFrozen() {
        return isFrozen;
    }

    public void setIsFrozen(String isFrozen) {
        this.isFrozen = isFrozen;
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
