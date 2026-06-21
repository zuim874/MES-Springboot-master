package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 工序信息实体
 */
@TableName("sp_process")
public class MesProcess extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工序编号（系统自动生成）
     */
    private String code;

    /**
     * 工序名称
     */
    private String name;

    /**
     * 加工单元ID
     */
    private String processUnitId;

    /**
     * 加工单元名称
     */
    private String processUnitName;

    /**
     * 工序工时（h），整数
     */
    private Integer laborHours;

    /**
     * 制造周期（h），整数，一般大于工序工时
     */
    private Integer manufacturingCycle;

    /**
     * 是否生成生产计划：是/否
     */
    private String generateProductionPlan;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除：0 未删除，1 已删除
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

    public String getProcessUnitId() {
        return processUnitId;
    }

    public void setProcessUnitId(String processUnitId) {
        this.processUnitId = processUnitId;
    }

    public String getProcessUnitName() {
        return processUnitName;
    }

    public void setProcessUnitName(String processUnitName) {
        this.processUnitName = processUnitName;
    }

    public Integer getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(Integer laborHours) {
        this.laborHours = laborHours;
    }

    public Integer getManufacturingCycle() {
        return manufacturingCycle;
    }

    public void setManufacturingCycle(Integer manufacturingCycle) {
        this.manufacturingCycle = manufacturingCycle;
    }

    public String getGenerateProductionPlan() {
        return generateProductionPlan;
    }

    public void setGenerateProductionPlan(String generateProductionPlan) {
        this.generateProductionPlan = generateProductionPlan;
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