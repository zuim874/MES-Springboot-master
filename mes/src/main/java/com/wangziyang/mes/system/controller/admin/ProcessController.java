package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.MesProcess;
import com.wangziyang.mes.system.entity.ProcessUnit;
import com.wangziyang.mes.system.service.IProcessService;
import com.wangziyang.mes.system.service.IProcessUnitService;
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

import java.util.List;

/**
 * 工序信息定义控制器
 */
@Controller
@RequestMapping("/admin/process")
public class ProcessController extends BaseController {

    @Autowired
    private IProcessService processService;

    @Autowired
    private IProcessUnitService processUnitService;

    @ApiOperation("工序信息定义列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/process/list";
    }

    @ApiOperation("工序信息分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "1") long current,
                       @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<MesProcess> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            wrapper.like("name", name);
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("code");
        IPage<MesProcess> page = processService.page(new Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @ApiOperation("获取所有加工单元列表（供下拉选择）")
    @GetMapping("/unit-list")
    @ResponseBody
    public Result unitList() {
        QueryWrapper<ProcessUnit> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("code");
        List<ProcessUnit> list = processUnitService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            MesProcess record = processService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/process/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result addOrUpdate(MesProcess record) {
        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }

        // 新增时自动生成工序编号
        if (StringUtils.isEmpty(record.getId())) {
            String code = generateProcessCode();
            record.setCode(code);
        }

        // 校验
        if (StringUtils.isEmpty(record.getName())) {
            return Result.failure("工序名称不能为空");
        }
        if (StringUtils.isEmpty(record.getProcessUnitId())) {
            return Result.failure("请选择加工单元");
        }
        if (record.getLaborHours() == null || record.getLaborHours() <= 0) {
            return Result.failure("工序工时必须大于0");
        }
        if (record.getManufacturingCycle() == null || record.getManufacturingCycle() <= 0) {
            return Result.failure("制造周期必须大于0");
        }
        if (record.getManufacturingCycle() <= record.getLaborHours()) {
            return Result.failure("制造周期必须大于工序工时");
        }
        if (StringUtils.isEmpty(record.getGenerateProductionPlan())) {
            return Result.failure("请选择是否生成生产计划");
        }

        // 回填加工单元名称
        ProcessUnit unit = processUnitService.getById(record.getProcessUnitId());
        if (unit != null) {
            record.setProcessUnitName(unit.getName());
        }

        processService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        MesProcess record = processService.getById(id);
        if (record == null) {
            return Result.failure("工序不存在");
        }
        record.setIsDeleted("1");
        processService.updateById(record);
        return Result.success();
    }

    /**
     * 自动生成工序编号 GX + 6位序号
     */
    private String generateProcessCode() {
        String prefix = "GX";
        QueryWrapper<MesProcess> wrapper = new QueryWrapper<>();
        wrapper.apply("code LIKE {0}", prefix + "%");
        wrapper.orderByDesc("code");
        wrapper.last("LIMIT 1");
        MesProcess max = processService.getOne(wrapper);
        int seq = 1;
        if (max != null && StringUtils.isNotEmpty(max.getCode())) {
            String numStr = max.getCode().substring(prefix.length());
            try {
                seq = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%06d", seq);
    }
}