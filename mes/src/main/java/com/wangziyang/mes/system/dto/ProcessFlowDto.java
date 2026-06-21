package com.wangziyang.mes.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wangziyang.mes.system.entity.ProcessFlow;

import java.util.List;

/**
 * 工序流程定义DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessFlowDto extends ProcessFlow {

    private static final long serialVersionUID = 1L;

    /** 选中的工序ID列表（按顺序） */
    private List<String> processIds;

    public List<String> getProcessIds() {
        return processIds;
    }

    public void setProcessIds(List<String> processIds) {
        this.processIds = processIds;
    }
}
