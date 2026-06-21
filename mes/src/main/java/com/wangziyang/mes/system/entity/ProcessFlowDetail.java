package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 工序流程明细实体
 */
@TableName("sp_process_flow_detail")
public class ProcessFlowDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 流程头ID */
    private String flowId;

    /** 工序ID */
    private String processId;

    /** 工序名称 */
    private String processName;

    /** 工序编号 */
    private String processCode;

    /** 排序号 */
    private Integer sortNum;

    /** 是否删除 */
    private String isDeleted;

    public String getFlowId() { return flowId; }
    public void setFlowId(String flowId) { this.flowId = flowId; }

    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getProcessCode() { return processCode; }
    public void setProcessCode(String processCode) { this.processCode = processCode; }

    public Integer getSortNum() { return sortNum; }
    public void setSortNum(Integer sortNum) { this.sortNum = sortNum; }

    public String getIsDeleted() { return isDeleted; }
    public void setIsDeleted(String isDeleted) { this.isDeleted = isDeleted; }
}
