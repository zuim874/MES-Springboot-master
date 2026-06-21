package com.wangziyang.mes.system.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.dto.SysMenuDTO;
import com.wangziyang.mes.system.entity.SysMenu;
import com.wangziyang.mes.system.entity.SysRole;
import com.wangziyang.mes.system.request.SysRolePageReq;
import com.wangziyang.mes.system.service.ISysMenuService;
import com.wangziyang.mes.system.service.ISysRoleService;
import com.wangziyang.mes.system.vo.TreeVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author SongPeng
 * @since 2019-10-16
 */
@Controller("adminSysRoleController")
@RequestMapping("/admin/sys/role")
public class SysRoleController extends BaseController {

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysMenuService sysMenuService;

    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/system/role/list";
    }

    @PostMapping("/page")
    @ResponseBody
    @RequiresPermissions("sys:role:view")
    public Result page(SysRolePageReq req) {
        QueryWrapper qw = new QueryWrapper();
        qw.orderByDesc(req.getOrderBy());
        IPage result = sysRoleService.page(req, qw);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SysRole record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SysRole result = sysRoleService.getById(record.getId());
            model.addAttribute("result", result);
        }
        return "admin/system/role/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @RequiresPermissions("sys:role:edit")
    public Result addOrUpdate(SysRole record) {
        sysRoleService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    /**
     * 角色授权菜单页面
     */
    @GetMapping("/auth-menu-ui")
    public String authMenuUI(Model model, String roleId) {
        model.addAttribute("roleId", roleId);
        return "admin/system/role/authMenu";
    }

    /**
     * 获取菜单树（带角色已授权状态）
     */
    @GetMapping("/menu-tree")
    @ResponseBody
    public Result menuTree(String roleId) throws Exception {
        // 获取所有菜单树
        List<TreeVO<SysMenu>> menuTree = sysMenuService.listMenuTree();

        // 获取该角色已授权的菜单ID列表
        List<SysMenuDTO> roleMenus = sysMenuService.listByRoleId(roleId);
        List<String> roleMenuIds = new ArrayList<>();
        if (roleMenus != null) {
            for (SysMenuDTO m : roleMenus) {
                roleMenuIds.add(m.getId());
            }
        }

        // 标记已授权菜单为选中状态
        setChecked(menuTree, roleMenuIds);

        return Result.success(menuTree);
    }

    private void setChecked(List<TreeVO<SysMenu>> treeList, List<String> roleMenuIds) {
        if (treeList == null || treeList.isEmpty()) {
            return;
        }
        for (TreeVO<SysMenu> node : treeList) {
            if (roleMenuIds.contains(node.getId())) {
                node.setChecked(true);
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                setChecked(node.getChildren(), roleMenuIds);
            }
        }
    }

    /**
     * 保存角色菜单授权
     */
    @PostMapping("/save-auth-menu")
    @ResponseBody
    @RequiresPermissions("sys:role:auth")
    public Result saveAuthMenu(String roleId, @RequestParam(value = "menuIds[]", required = false) List<String> menuIds) throws Exception {
        sysRoleService.saveAuthMenu(roleId, menuIds);
        return Result.success();
    }
}
