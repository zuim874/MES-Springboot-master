package com.wangziyang.mes.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wangziyang.mes.common.BaseEntity;

/**
 * 工序流程定义实体
 */
@TableName("sp_process_flow")
public class ProcessFlow extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 流程编码 */
    private String code;

    /** 流程名称 */
    private String name;

    /** 时序流程字符串（如：装配工序->测试工序->...） */
    private String processChain;

    /** 备注 */
    private String remark;

    /** 是否删除 */
    private String isDeleted;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProcessChain() { return processChain; }
    public void setProcessChain(String processChain) { this.processChain = processChain; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getIsDeleted() { return isDeleted; }
    public void setIsDeleted(String isDeleted) { this.isDeleted = isDeleted; }
}
