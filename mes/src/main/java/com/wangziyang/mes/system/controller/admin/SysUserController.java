package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.dto.SysRoleDTO;
import com.wangziyang.mes.system.dto.SysUserDTO;
import com.wangziyang.mes.system.entity.SysDepartment;
import com.wangziyang.mes.system.entity.SysUser;
import com.wangziyang.mes.system.request.SysUserPageReq;
import com.wangziyang.mes.system.service.ISysDepartmentService;
import com.wangziyang.mes.system.service.ISysRoleService;
import com.wangziyang.mes.system.service.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
 * <p>
 * 前端控制器
 * </p>
 *
 * @author SongPeng
 * @since 2019-10-15
 */
@Controller("adminSysUserController")
@RequestMapping("/admin/sys/user")
public class SysUserController extends BaseController {

    Logger logger = LoggerFactory.getLogger(SysUserController.class);

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysDepartmentService sysDepartmentService;

    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/system/user/list";
    }

    @PostMapping("/page")
    @ResponseBody
    @RequiresPermissions("sys:user:view")
    public Result page(SysUserPageReq req) throws Exception {
        QueryWrapper qw = new QueryWrapper();
        if (StringUtils.isNotEmpty(req.getNameLike())) {
            qw.likeRight("name", req.getNameLike());
        }
        if (StringUtils.isNotEmpty(req.getUsernameLike())) {
            qw.likeRight("username", req.getUsernameLike());
        }
        qw.orderByDesc(req.getOrderBy());
        IPage page = sysUserService.page(req, qw);

        // 补充部门名称和角色名称
        List<SysUser> records = page.getRecords();
        List<Map<String, Object>> newRecords = new ArrayList<>();
        for (SysUser user : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getName());
            map.put("username", user.getUsername());
            map.put("deptId", user.getDeptId());
            map.put("email", user.getEmail());
            map.put("mobile", user.getMobile());
            map.put("tel", user.getTel());
            map.put("sex", user.getSex());
            map.put("descr", user.getDescr());
            map.put("deleted", user.getDeleted());
            map.put("createTime", user.getCreateTime());
            map.put("createUsername", user.getCreateUsername());

            // 查询部门名称
            if (StringUtils.isNotEmpty(user.getDeptId())) {
                SysDepartment dept = sysDepartmentService.getById(user.getDeptId());
                map.put("deptName", dept != null ? dept.getName() : "");
            } else {
                map.put("deptName", "");
            }

            // 查询角色名称
            SysUserDTO tempDto = new SysUserDTO();
            tempDto.setId(user.getId());
            List<SysRoleDTO> roles = sysRoleService.listByUserId(tempDto.getId());
            String roleNames = roles.stream()
                    .filter(SysRoleDTO::getChecked)
                    .map(SysRoleDTO::getName)
                    .collect(Collectors.joining(","));
            map.put("roleNames", roleNames);

            newRecords.add(map);
        }
        page.setRecords(newRecords);
        return Result.success(page);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(SysUser record, Model model) throws Exception {
        if (StringUtils.isNotEmpty(record.getId())) {
            SysUser result = sysUserService.getById(record.getId());
            model.addAttribute("result", result);
        }
        List<SysRoleDTO> sysRoles = sysRoleService.listByUserId(record.getId());
        model.addAttribute("sysRoles", sysRoles);
        return "admin/system/user/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @RequiresPermissions("sys:user:edit")
    public Result addOrUpdate(SysUserDTO record) throws Exception {
        if (StringUtils.isEmpty(record.getId())) {
            sysUserService.save(record);
        } else {
            // 编辑时如果密码为空，则不更新密码，避免重复加密
            if (StringUtils.isEmpty(record.getPassword())) {
                SysUser exist = sysUserService.getById(record.getId());
                if (exist != null) {
                    record.setPassword(exist.getPassword());
                }
            }
            sysUserService.update(record);
        }
        return Result.success(record.getId());
    }

    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("sys:user:delete")
    public Result delete(@RequestParam String id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.failure("用户不存在");
        }
        user.setDeleted("1");
        sysUserService.updateById(user);
        return Result.success();
    }
}
