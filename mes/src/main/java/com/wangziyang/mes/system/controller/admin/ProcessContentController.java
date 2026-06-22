package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.ProcessContent;
import com.wangziyang.mes.system.entity.ProcessFlow;
import com.wangziyang.mes.system.entity.ProcessPlan;
import com.wangziyang.mes.system.entity.ProductBom;
import com.wangziyang.mes.system.entity.ProductBomNode;
import com.wangziyang.mes.system.service.IProcessContentService;
import com.wangziyang.mes.system.service.IProcessFlowService;
import com.wangziyang.mes.system.service.IProcessPlanService;
import com.wangziyang.mes.system.service.IProductBomNodeService;
import com.wangziyang.mes.system.service.IProductBomService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工艺内容编制控制器
 */
@Controller
@RequestMapping("/admin/processContent")
public class ProcessContentController extends BaseController {

    @Autowired
    private IProductBomService bomService;

    @Autowired
    private IProductBomNodeService nodeService;

    @Autowired
    private IProcessPlanService processPlanService;

    @Autowired
    private IProcessContentService processContentService;

    @Autowired
    private IProcessFlowService processFlowService;

    // ==================== 页面 ====================

    @ApiOperation("工艺内容编制列表UI")
    @GetMapping("/list-ui")
    public String listUI() {
        return "admin/processContent/list";
    }

    @ApiOperation("工艺内容编制详情UI")
    @GetMapping("/detail-ui")
    public String detailUI(Model model, @RequestParam String bomId) {
        ProductBom bom = bomService.getById(bomId);
        if (bom != null) {
            model.addAttribute("bom", bom);
            model.addAttribute("bomId", bom.getId());
            model.addAttribute("bomName", bom.getName());
            model.addAttribute("processPlanLocked", bom.getProcessPlanLocked() != null ? bom.getProcessPlanLocked() : "0");
        } else {
            model.addAttribute("bomId", bomId);
            model.addAttribute("bomName", "未知");
            model.addAttribute("processPlanLocked", "0");
        }
        return "admin/processContent/detail";
    }

    // ==================== 数据接口 ====================

    @ApiOperation("BOM产品分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "1") long current,
                       @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<ProductBom> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            wrapper.like("name", name);
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByDesc("create_time");
        IPage<ProductBom> page = bomService.page(new Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @ApiOperation("获取BOM节点列表（含工艺规划及工艺内容编制状态）")
    @PostMapping("/node-list")
    @ResponseBody
    public Result nodeList(@RequestParam String bomId) {
        QueryWrapper<ProductBomNode> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", bomId);
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("node_level", "sort_num", "node_code");
        List<ProductBomNode> nodes = nodeService.list(wrapper);

        // 查询所有工艺规划
        QueryWrapper<ProcessPlan> planWrapper = new QueryWrapper<>();
        planWrapper.eq("bom_id", bomId);
        planWrapper.eq("is_deleted", "0");
        List<ProcessPlan> plans = processPlanService.list(planWrapper);

        Map<String, ProcessPlan> planMap = plans.stream()
                .filter(p -> StringUtils.isNotEmpty(p.getBomNodeId()))
                .collect(Collectors.toMap(ProcessPlan::getBomNodeId, p -> p, (a, b) -> a));

        // 查询所有工艺内容编制
        QueryWrapper<ProcessContent> contentWrapper = new QueryWrapper<>();
        contentWrapper.eq("bom_id", bomId);
        contentWrapper.eq("is_deleted", "0");
        List<ProcessContent> contents = processContentService.list(contentWrapper);

        Map<String, ProcessContent> contentMap = contents.stream()
                .filter(c -> StringUtils.isNotEmpty(c.getBomNodeId()))
                .collect(Collectors.toMap(ProcessContent::getBomNodeId, c -> c, (a, b) -> a));

        // 组装返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductBomNode node : nodes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", node.getId());
            item.put("parentId", node.getParentId());
            item.put("bomId", node.getBomId());
            item.put("nodeCode", node.getNodeCode());
            item.put("nodeName", node.getNodeName());
            item.put("nodeLevel", node.getNodeLevel());
            item.put("nodeType", node.getNodeType());
            item.put("qty", node.getQty());

            ProcessPlan plan = planMap.get(node.getId());
            if (plan != null) {
                item.put("planId", plan.getId());
                item.put("flowId", plan.getFlowId());
                // 工序流程定义内已包含所有零散工序，此处展示流程名称即可
                if (StringUtils.isNotEmpty(plan.getFlowId())) {
                    ProcessFlow flow = processFlowService.getById(plan.getFlowId());
                    if (flow != null) {
                        item.put("flowName", flow.getName());
                    }
                }
                // 保留 processId/processName/processCode 以兼容历史数据
                item.put("processId", plan.getProcessId());
                item.put("processName", plan.getProcessName());
                item.put("processCode", plan.getProcessCode());
            }

            ProcessContent content = contentMap.get(node.getId());
            if (content != null) {
                item.put("contentId", content.getId());
                item.put("hasContent", true);
            } else {
                item.put("hasContent", false);
            }

            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("获取节点工艺内容")
    @GetMapping("/get-content")
    @ResponseBody
    public Result getContent(@RequestParam String bomNodeId) {
        QueryWrapper<ProcessContent> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_node_id", bomNodeId);
        wrapper.eq("is_deleted", "0");
        wrapper.last("LIMIT 1");
        ProcessContent content = processContentService.getOne(wrapper);
        return Result.success(content);
    }

    @ApiOperation("保存工艺内容")
    @PostMapping("/save")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result save(ProcessContent record) {
        if (StringUtils.isEmpty(record.getBomId())) {
            return Result.failure("BOM ID不能为空");
        }
        if (StringUtils.isEmpty(record.getBomNodeId())) {
            return Result.failure("BOM节点ID不能为空");
        }

        // 检查工艺流程是否已锁定（必须先锁定工艺才能编制内容）
        ProductBom bom = bomService.getById(record.getBomId());
        if (bom == null) {
            return Result.failure("BOM不存在");
        }
        if (!"1".equals(bom.getProcessPlanLocked())) {
            return Result.failure("该产品工艺尚未锁定，无法编制工艺内容，请先完成工艺流程管理并锁定工艺");
        }

        // 检查该节点是否有工艺规划（必须先绑定工序流程定义）
        QueryWrapper<ProcessPlan> planWrapper = new QueryWrapper<>();
        planWrapper.eq("bom_id", record.getBomId());
        planWrapper.eq("bom_node_id", record.getBomNodeId());
        planWrapper.eq("is_deleted", "0");
        ProcessPlan plan = processPlanService.getOne(planWrapper);
        if (plan == null) {
            return Result.failure("该BOM节点尚未绑定工序流程定义，请先完成工艺流程管理");
        }
        if (StringUtils.isEmpty(plan.getFlowId())) {
            return Result.failure("该BOM节点尚未绑定工序流程定义，请先完成工艺流程管理");
        }

        record.setProcessPlanId(plan.getId());
        // processId 字段改为保存 flowId（工序流程定义ID）
        record.setProcessId(plan.getFlowId());

        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }

        // 检查是否已有该节点的工艺内容
        QueryWrapper<ProcessContent> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", record.getBomId());
        wrapper.eq("bom_node_id", record.getBomNodeId());
        wrapper.eq("is_deleted", "0");
        ProcessContent existing = processContentService.getOne(wrapper);

        if (existing != null) {
            record.setId(existing.getId());
        }

        processContentService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @ApiOperation("删除工艺内容")
    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        ProcessContent content = processContentService.getById(id);
        if (content == null) {
            return Result.failure("记录不存在");
        }
        // 检查工艺流程是否已锁定
        ProductBom bom = bomService.getById(content.getBomId());
        if (bom != null && "1".equals(bom.getProcessPlanLocked())) {
            return Result.failure("该产品工艺规划已锁定，无法删除工艺内容");
        }
        content.setIsDeleted("1");
        processContentService.updateById(content);
        return Result.success();
    }
}
