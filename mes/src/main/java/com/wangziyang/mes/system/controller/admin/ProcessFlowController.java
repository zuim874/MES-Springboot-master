package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.dto.ProcessFlowDto;
import com.wangziyang.mes.system.entity.MesProcess;
import com.wangziyang.mes.system.entity.ProcessFlow;
import com.wangziyang.mes.system.entity.ProcessFlowDetail;
import com.wangziyang.mes.system.service.IProcessFlowDetailService;
import com.wangziyang.mes.system.service.IProcessFlowService;
import com.wangziyang.mes.system.service.IProcessService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工序流程定义控制器
 */
@Controller
@RequestMapping("/admin/processFlow")
public class ProcessFlowController extends BaseController {

    @Autowired
    private IProcessFlowService processFlowService;

    @Autowired
    private IProcessFlowDetailService processFlowDetailService;

    @Autowired
    private IProcessService processService;

    @ApiOperation("工序流程定义列表UI")
    @GetMapping("/list-ui")
    public String listUI() {
        return "admin/processFlow/list";
    }

    @ApiOperation("工序流程定义编辑UI")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        // 全部工序（用于transfer选择）
        QueryWrapper<MesProcess> processWrapper = new QueryWrapper<>();
        processWrapper.eq("is_deleted", "0");
        processWrapper.orderByAsc("code");
        List<MesProcess> allProcesses = processService.list(processWrapper);
        List<Map<String, String>> allProcessList = new ArrayList<>();
        for (MesProcess p : allProcesses) {
            Map<String, String> item = new HashMap<>();
            item.put("value", p.getId());
            item.put("title", p.getName() + " (" + p.getCode() + ")");
            allProcessList.add(item);
        }
        model.addAttribute("allProcess", allProcessList);

        // 当前流程及已选工序
        if (StringUtils.isNotEmpty(id)) {
            ProcessFlow flow = processFlowService.getById(id);
            model.addAttribute("flow", flow);

            QueryWrapper<ProcessFlowDetail> detailWrapper = new QueryWrapper<>();
            detailWrapper.eq("flow_id", id);
            detailWrapper.eq("is_deleted", "0");
            detailWrapper.orderByAsc("sort_num");
            List<ProcessFlowDetail> details = processFlowDetailService.list(detailWrapper);
            List<String> currentProcessIds = new ArrayList<>();
            for (ProcessFlowDetail d : details) {
                currentProcessIds.add(d.getProcessId());
            }
            model.addAttribute("currentProcessIds", currentProcessIds);
        }
        return "admin/processFlow/addOrUpdate";
    }

    @ApiOperation("工序流程分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "1") long current,
                       @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<ProcessFlow> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            wrapper.and(w -> w.like("code", name).or().like("name", name));
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByDesc("create_time");
        IPage<ProcessFlow> page = processFlowService.page(new Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @ApiOperation("保存工序流程")
    @PostMapping("/add-or-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result addOrUpdate(@RequestBody ProcessFlowDto dto) {
        if (StringUtils.isEmpty(dto.getCode())) {
            return Result.failure("流程编码不能为空");
        }
        if (StringUtils.isEmpty(dto.getName())) {
            return Result.failure("流程名称不能为空");
        }
        if (dto.getProcessIds() == null || dto.getProcessIds().isEmpty()) {
            return Result.failure("请至少选择一个工序");
        }

        // 生成时序流程字符串
        List<String> processNames = new ArrayList<>();
        for (int i = 0; i < dto.getProcessIds().size(); i++) {
            String pid = dto.getProcessIds().get(i);
            MesProcess mp = processService.getById(pid);
            if (mp != null) {
                processNames.add(mp.getName());
            }
        }
        dto.setProcessChain(String.join("->", processNames));

        if (StringUtils.isEmpty(dto.getIsDeleted())) {
            dto.setIsDeleted("0");
        }

        // 保存/更新头表
        processFlowService.saveOrUpdate(dto);
        String flowId = dto.getId();

        // 删除旧的明细
        QueryWrapper<ProcessFlowDetail> delWrapper = new QueryWrapper<>();
        delWrapper.eq("flow_id", flowId);
        processFlowDetailService.remove(delWrapper);

        // 保存新的明细
        List<ProcessFlowDetail> details = new ArrayList<>();
        for (int i = 0; i < dto.getProcessIds().size(); i++) {
            String pid = dto.getProcessIds().get(i);
            MesProcess mp = processService.getById(pid);
            ProcessFlowDetail detail = new ProcessFlowDetail();
            detail.setFlowId(flowId);
            detail.setProcessId(pid);
            if (mp != null) {
                detail.setProcessName(mp.getName());
                detail.setProcessCode(mp.getCode());
            }
            detail.setSortNum(i + 1);
            detail.setIsDeleted("0");
            details.add(detail);
        }
        processFlowDetailService.saveBatch(details);
        return Result.success(flowId);
    }

    @ApiOperation("删除工序流程")
    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        ProcessFlow flow = processFlowService.getById(id);
        if (flow == null) {
            return Result.failure("记录不存在");
        }
        flow.setIsDeleted("1");
        processFlowService.updateById(flow);

        QueryWrapper<ProcessFlowDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("flow_id", id);
        List<ProcessFlowDetail> details = processFlowDetailService.list(wrapper);
        for (ProcessFlowDetail d : details) {
            d.setIsDeleted("1");
        }
        processFlowDetailService.updateBatchById(details);
        return Result.success();
    }

    @ApiOperation("获取流程明细")
    @GetMapping("/detail")
    @ResponseBody
    public Result detail(@RequestParam String id) {
        ProcessFlow flow = processFlowService.getById(id);
        if (flow == null) {
            return Result.failure("记录不存在");
        }
        QueryWrapper<ProcessFlowDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("flow_id", id);
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("sort_num");
        List<ProcessFlowDetail> details = processFlowDetailService.list(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("flow", flow);
        result.put("details", details);
        return Result.success(result);
    }
}
