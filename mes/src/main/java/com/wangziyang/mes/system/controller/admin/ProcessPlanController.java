package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.ProcessFlow;
import com.wangziyang.mes.system.entity.ProcessPlan;
import com.wangziyang.mes.system.entity.ProductBom;
import com.wangziyang.mes.system.entity.ProductBomNode;
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
 * 工艺流程管理控制器
 */
@Controller
@RequestMapping("/admin/processPlan")
public class ProcessPlanController extends BaseController {

    @Autowired
    private IProductBomService bomService;

    @Autowired
    private IProductBomNodeService nodeService;

    @Autowired
    private IProcessPlanService processPlanService;

    @Autowired
    private IProcessFlowService processFlowService;

    // ==================== 页面 ====================

    @ApiOperation("工艺流程管理列表UI")
    @GetMapping("/list-ui")
    public String listUI() {
        return "admin/processPlan/list";
    }

    @ApiOperation("工艺流程管理详情UI")
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

        // 查询根节点，如果不存在则自动创建（确保BOM树至少有一个根节点可供展示）
        QueryWrapper<ProductBomNode> rootWrapper = new QueryWrapper<>();
        rootWrapper.eq("bom_id", bomId);
        rootWrapper.eq("is_deleted", "0");
        rootWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", ""));
        rootWrapper.last("LIMIT 1");
        ProductBomNode rootNode = nodeService.getOne(rootWrapper);
        if (rootNode == null && bom != null) {
            rootNode = new ProductBomNode();
            rootNode.setBomId(bomId);
            rootNode.setParentId(null);
            rootNode.setNodeCode("0");
            rootNode.setNodeName(bom.getName());
            rootNode.setNodeLevel(0);
            rootNode.setNodeType("0");
            rootNode.setQty(java.math.BigDecimal.ONE);
            rootNode.setSortNum(0);
            rootNode.setIsDeleted("0");
            nodeService.save(rootNode);
        }

        return "admin/processPlan/detail";
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

    @ApiOperation("获取BOM节点列表（含工艺规划信息）")
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

        // nodeId -> ProcessPlan
        Map<String, ProcessPlan> planMap = plans.stream()
                .filter(p -> StringUtils.isNotEmpty(p.getBomNodeId()))
                .collect(Collectors.toMap(ProcessPlan::getBomNodeId, p -> p, (a, b) -> a));

        // planId -> ProcessPlan（用于查找上级工艺）
        Map<String, ProcessPlan> planIdMap = plans.stream()
                .collect(Collectors.toMap(ProcessPlan::getId, p -> p, (a, b) -> a));

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
                item.put("processId", plan.getProcessId());
                item.put("processName", plan.getProcessName());
                item.put("processCode", plan.getProcessCode());

                // 上级工艺
                if (StringUtils.isNotEmpty(plan.getParentId())) {
                    ProcessPlan parentPlan = planIdMap.get(plan.getParentId());
                    if (parentPlan != null) {
                        item.put("parentProcessName", parentPlan.getProcessName());
                    }
                }
            }

            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("获取所有工序流程定义列表（供下拉选择）")
    @GetMapping("/flow-list")
    @ResponseBody
    public Result flowList() {
        QueryWrapper<ProcessFlow> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", "0");
        wrapper.orderByDesc("create_time");
        List<ProcessFlow> list = processFlowService.list(wrapper);
        return Result.success(list);
    }

    @ApiOperation("获取节点的工艺规划信息")
    @GetMapping("/get-plan")
    @ResponseBody
    public Result getPlan(@RequestParam String bomNodeId) {
        QueryWrapper<ProcessPlan> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_node_id", bomNodeId);
        wrapper.eq("is_deleted", "0");
        wrapper.last("LIMIT 1");
        ProcessPlan plan = processPlanService.getOne(wrapper);
        return Result.success(plan);
    }

    @ApiOperation("获取上级工艺规划信息")
    @GetMapping("/parent-plan")
    @ResponseBody
    public Result parentPlan(@RequestParam String bomNodeId, @RequestParam String bomId) {
        // 查询当前节点
        ProductBomNode currentNode = nodeService.getById(bomNodeId);
        if (currentNode == null || StringUtils.isEmpty(currentNode.getParentId())) {
            return Result.success(null);
        }

        // 查询父节点的工艺规划
        QueryWrapper<ProcessPlan> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", bomId);
        wrapper.eq("bom_node_id", currentNode.getParentId());
        wrapper.eq("is_deleted", "0");
        wrapper.last("LIMIT 1");
        ProcessPlan plan = processPlanService.getOne(wrapper);
        return Result.success(plan);
    }

    @ApiOperation("保存工艺规划")
    @PostMapping("/save")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result save(ProcessPlan record) {
        if (StringUtils.isEmpty(record.getBomId())) {
            return Result.failure("BOM ID不能为空");
        }
        if (StringUtils.isEmpty(record.getBomNodeId())) {
            return Result.failure("BOM节点ID不能为空");
        }
        if (StringUtils.isEmpty(record.getFlowId())) {
            return Result.failure("请选择工序流程定义");
        }

        // 物料类型节点无需绑定工艺
        ProductBomNode bomNode = nodeService.getById(record.getBomNodeId());
        if (bomNode != null && "2".equals(bomNode.getNodeType())) {
            return Result.failure("物料节点无需绑定工艺");
        }

        // 检查是否已锁定
        ProductBom bom = bomService.getById(record.getBomId());
        if (bom != null && "1".equals(bom.getProcessPlanLocked())) {
            return Result.failure("该产品工艺规划已锁定，无法编辑");
        }

        // 工序流程定义内已包含零散工序，此处不再单独回填 processId/processName/processCode
        record.setProcessId(null);
        record.setProcessName(null);
        record.setProcessCode(null);

        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }

        // 检查是否已有该节点的工艺规划
        QueryWrapper<ProcessPlan> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", record.getBomId());
        wrapper.eq("bom_node_id", record.getBomNodeId());
        wrapper.eq("is_deleted", "0");
        ProcessPlan existing = processPlanService.getOne(wrapper);

        if (existing != null) {
            record.setId(existing.getId());
        }

        processPlanService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @ApiOperation("锁定工艺规划")
    @PostMapping("/lock")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result lock(@RequestParam String bomId) {
        ProductBom bom = bomService.getById(bomId);
        if (bom == null) {
            return Result.failure("BOM不存在");
        }
        bom.setProcessPlanLocked("1");
        bomService.updateById(bom);
        return Result.success();
    }

    @ApiOperation("解锁工艺规划")
    @PostMapping("/unlock")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result unlock(@RequestParam String bomId) {
        ProductBom bom = bomService.getById(bomId);
        if (bom == null) {
            return Result.failure("BOM不存在");
        }
        bom.setProcessPlanLocked("0");
        bomService.updateById(bom);
        return Result.success();
    }

    @ApiOperation("删除工艺规划")
    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        ProcessPlan plan = processPlanService.getById(id);
        if (plan == null) {
            return Result.failure("工艺规划不存在");
        }

        // 检查是否已锁定
        ProductBom bom = bomService.getById(plan.getBomId());
        if (bom != null && "1".equals(bom.getProcessPlanLocked())) {
            return Result.failure("该产品工艺规划已锁定，无法删除");
        }

        plan.setIsDeleted("1");
        processPlanService.updateById(plan);
        return Result.success();
    }
}