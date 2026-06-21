package com.wangziyang.mes.system.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.SysDepartment;
import com.wangziyang.mes.system.entity.SysDict;
import com.wangziyang.mes.system.request.SysDepartmentPageReq;
import com.wangziyang.mes.system.service.ISysDepartmentService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>
 * 系统部门前端控制器
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
@Controller
@RequestMapping("/admin/sys/department")
public class SysDepartmentController extends BaseController {

    Logger logger = LoggerFactory.getLogger(SysDepartmentController.class);

    @Autowired
    private ISysDepartmentService sysDepartmentService;

    @ApiOperation("系统部门信息列表UI")
    @ApiImplicitParams({@ApiImplicitParam(name = "model", value = "模型", defaultValue = "模型")})
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/system/department/list";
    }

    @ApiOperation("系统部门信息分页列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "模型", defaultValue = "模型")})
    @PostMapping("/page")
    @ResponseBody
    @RequiresPermissions("sys:dept:view")
    public Result page(SysDepartmentPageReq req) {
        IPage result = sysDepartmentService.page(req);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SysDepartment record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SysDepartment sysDepartment = sysDepartmentService.getById(record.getId());
            model.addAttribute("result", sysDepartment);
        }
        return "admin/system/department/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @RequiresPermissions("sys:dept:edit")
    public Result addOrUpdate(SysDepartment record) {
        sysDepartmentService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    /**
     * 查询所有部门列表（下拉选择用）
     */
    @GetMapping("/list")
    @ResponseBody
    @RequiresPermissions("sys:dept:view")
    public Result list() {
        List<SysDepartment> list = sysDepartmentService.list();
        return Result.success(list);
    }

    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("sys:dept:delete")
    public Result delete(@org.springframework.web.bind.annotation.RequestParam String id) {
        SysDepartment dept = sysDepartmentService.getById(id);
        if (dept == null) {
            return Result.failure("部门不存在");
        }
        dept.setIsDeleted("1");
        sysDepartmentService.updateById(dept);
        return Result.success();
    }
}
