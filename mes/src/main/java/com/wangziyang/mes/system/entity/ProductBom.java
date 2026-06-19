package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 产品BOM主表实体
 */
@TableName("sp_product_bom")
public class ProductBom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * BOM编码
     */
    private String bomCode;

    /**
     * 产品物料编码
     */
    private String productMaterielCode;

    /**
     * 产品物料描述
     */
    private String productMaterielDesc;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态 0草稿 1定版
     */
    private String state;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除
     */
    private String isDeleted;

    public String getBomCode() {
        return bomCode;
    }

    public void setBomCode(String bomCode) {
        this.bomCode = bomCode;
    }

    public String getProductMaterielCode() {
        return productMaterielCode;
    }

    public void setProductMaterielCode(String productMaterielCode) {
        this.productMaterielCode = productMaterielCode;
    }

    public String getProductMaterielDesc() {
        return productMaterielDesc;
    }

    public void setProductMaterielDesc(String productMaterielDesc) {
        this.productMaterielDesc = productMaterielDesc;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
