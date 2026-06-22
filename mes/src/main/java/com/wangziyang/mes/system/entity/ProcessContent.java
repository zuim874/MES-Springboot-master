package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 工艺内容编制实体
 */
@TableName("sp_process_content")
public class ProcessContent extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * BOM头ID
     */
    private String bomId;

    /**
     * BOM节点ID
     */
    private String bomNodeId;

    /**
     * 工艺规划ID
     */
    private String processPlanId;

    /**
     * 工序流程定义ID（原工序ID字段，现改为存储flowId）
     */
    private String processId;

    /**
     * 操作步骤
     */
    private String operationSteps;

    /**
     * 设备工装
     */
    private String equipmentTools;

    /**
     * 所需物料清单
     */
    private String materialList;

    /**
     * 技术参数
     */
    private String techParams;

    /**
     * 自检标准
     */
    private String selfCheckStd;

    /**
     * 安全/防静电要求
     */
    private String safetyReq;

    /**
     * 工艺附图（JSON数组）
     */
    private String processImages;

    /**
     * 技术文档（JSON数组）
     */
    private String techDocs;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
     */
    private String isDeleted;

    public String getBomId() {
        return bomId;
    }

    public void setBomId(String bomId) {
        this.bomId = bomId;
    }

    public String getBomNodeId() {
        return bomNodeId;
    }

    public void setBomNodeId(String bomNodeId) {
        this.bomNodeId = bomNodeId;
    }

    public String getProcessPlanId() {
        return processPlanId;
    }

    public void setProcessPlanId(String processPlanId) {
        this.processPlanId = processPlanId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getOperationSteps() {
        return operationSteps;
    }

    public void setOperationSteps(String operationSteps) {
        this.operationSteps = operationSteps;
    }

    public String getEquipmentTools() {
        return equipmentTools;
    }

    public void setEquipmentTools(String equipmentTools) {
        this.equipmentTools = equipmentTools;
    }

    public String getMaterialList() {
        return materialList;
    }

    public void setMaterialList(String materialList) {
        this.materialList = materialList;
    }

    public String getTechParams() {
        return techParams;
    }

    public void setTechParams(String techParams) {
        this.techParams = techParams;
    }

    public String getSelfCheckStd() {
        return selfCheckStd;
    }

    public void setSelfCheckStd(String selfCheckStd) {
        this.selfCheckStd = selfCheckStd;
    }

    public String getSafetyReq() {
        return safetyReq;
    }

    public void setSafetyReq(String safetyReq) {
        this.safetyReq = safetyReq;
    }

    public String getProcessImages() {
        return processImages;
    }

    public void setProcessImages(String processImages) {
        this.processImages = processImages;
    }

    public String getTechDocs() {
        return techDocs;
    }

    public void setTechDocs(String techDocs) {
        this.techDocs = techDocs;
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
