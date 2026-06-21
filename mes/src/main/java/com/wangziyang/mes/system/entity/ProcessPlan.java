package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 工艺规划实体
 */
@TableName("sp_process_plan")
public class ProcessPlan extends BaseEntity {

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
     * 上级工艺规划ID
     */
    private String parentId;

    /**
     * 工序流程定义ID
     */
    private String flowId;

    /**
     * 工序ID
     */
    private String processId;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 工序编号
     */
    private String processCode;

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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}