package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.ProcessUnit;
import com.wangziyang.mes.system.entity.WorkTeam;
import com.wangziyang.mes.system.mapper.ProcessUnitTeamMapper;
import com.wangziyang.mes.system.request.ProcessUnitPageReq;
import com.wangziyang.mes.system.service.IProcessUnitService;
import com.wangziyang.mes.system.service.IProcessUnitTeamService;
import com.wangziyang.mes.system.service.IWorkTeamService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>
 * 加工单元前端控制器
 * </p>
 */
@Controller
@RequestMapping("/admin/process/unit")
public class ProcessUnitController extends BaseController {

    @Autowired
    private IProcessUnitService processUnitService;

    @Autowired
    private IProcessUnitTeamService processUnitTeamService;

    @Autowired
    private ProcessUnitTeamMapper processUnitTeamMapper;

    @Autowired
    private IWorkTeamService workTeamService;

    @ApiOperation("加工单元列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/process/unit/list";
    }

    @ApiOperation("加工单元分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(ProcessUnitPageReq req) {
        QueryWrapper<ProcessUnit> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getName())) {
            wrapper.like("name", req.getName());
        }
        if (StringUtils.isNotEmpty(req.getCode())) {
            wrapper.like("code", req.getCode());
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("code");
        IPage result = processUnitService.page(req, wrapper);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            ProcessUnit record = processUnitService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/process/unit/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(ProcessUnit record, String isDeleted) {
        if (StringUtils.isNotEmpty(isDeleted)) {
            record.setIsDeleted(isDeleted);
        }
        processUnitService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @GetMapping("/bind-team-ui")
    public String bindTeamUI(Model model, @RequestParam(required = false) String processUnitId) {
        model.addAttribute("processUnitId", processUnitId);
        return "admin/process/unit/bindTeam";
    }

    @GetMapping("/team-list")
    @ResponseBody
    public Result teamList() {
        QueryWrapper<WorkTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", "0");
        List<WorkTeam> list = workTeamService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/bind-team-ids")
    @ResponseBody
    public Result bindTeamIds(@RequestParam String processUnitId) {
        List<String> teamIds = processUnitTeamMapper.selectTeamIdsByProcessUnitId(processUnitId);
        return Result.success(teamIds);
    }

    @PostMapping("/save-bind-teams")
    @ResponseBody
    public Result saveBindTeams(@RequestParam String processUnitId, @RequestParam(value = "teamIds", required = false) List<String> teamIds) {
        if (StringUtils.isEmpty(processUnitId)) {
            return Result.failure("加工单元ID不能为空");
        }
        processUnitTeamService.saveBindTeams(processUnitId, teamIds);
        return Result.success();
    }
}
