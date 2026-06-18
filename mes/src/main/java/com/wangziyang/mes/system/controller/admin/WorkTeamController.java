package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.SysUser;
import com.wangziyang.mes.system.entity.WorkTeam;
import com.wangziyang.mes.system.entity.WorkTeamUser;
import com.wangziyang.mes.system.mapper.WorkTeamUserMapper;
import com.wangziyang.mes.system.request.WorkTeamPageReq;
import com.wangziyang.mes.system.service.ISysUserService;
import com.wangziyang.mes.system.service.IWorkTeamService;
import com.wangziyang.mes.system.service.IWorkTeamUserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

/**
 * <p>
 * 生产班组前端控制器
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
@Controller
@RequestMapping("/admin/work/team")
public class WorkTeamController extends BaseController {

    Logger logger = LoggerFactory.getLogger(WorkTeamController.class);

    @Autowired
    private IWorkTeamService workTeamService;

    @Autowired
    private IWorkTeamUserService workTeamUserService;

    @Autowired
    private WorkTeamUserMapper workTeamUserMapper;

    @Autowired
    private ISysUserService sysUserService;

    @ApiOperation("生产班组列表UI")
    @ApiImplicitParams({@ApiImplicitParam(name = "model", value = "模型", defaultValue = "模型")})
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/work/team/list";
    }

    @ApiOperation("生产班组分页列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "模型", defaultValue = "模型")})
    @PostMapping("/page")
    @ResponseBody
    public Result page(WorkTeamPageReq req) {
        QueryWrapper<WorkTeam> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getName())) {
            wrapper.like("name", req.getName());
        }
        if (StringUtils.isNotEmpty(req.getCode())) {
            wrapper.like("code", req.getCode());
        }
        wrapper.orderByAsc("code");
        IPage result = workTeamService.page(req, wrapper);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, WorkTeam record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            WorkTeam team = workTeamService.getById(record.getId());
            model.addAttribute("result", team);
        }
        return "admin/work/team/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(WorkTeam record) {
        workTeamService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @GetMapping("/bind-user-ui")
    public String bindUserUI(Model model, String teamId) {
        model.addAttribute("teamId", teamId);
        return "admin/work/team/bindUser";
    }

    /**
     * 查询所有用户列表（员工绑定弹窗用）
     */
    @GetMapping("/user-list")
    @ResponseBody
    public Result userList() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", "0");
        List<SysUser> list = sysUserService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 查询班组已绑定的用户ID列表
     */
    @GetMapping("/bind-user-ids")
    @ResponseBody
    public Result bindUserIds(String teamId) {
        List<String> userIds = workTeamUserMapper.selectUserIdsByTeamId(teamId);
        return Result.success(userIds);
    }

    /**
     * 保存班组员工绑定
     */
    @PostMapping("/save-bind-users")
    @ResponseBody
    public Result saveBindUsers(String teamId, @RequestParam(value = "userIds[]", required = false) List<String> userIds) {
        workTeamUserService.saveBindUsers(teamId, userIds);
        return Result.success();
    }
}
