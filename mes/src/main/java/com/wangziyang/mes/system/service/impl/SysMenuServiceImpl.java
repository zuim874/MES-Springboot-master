package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.common.util.TreeUtil;
import com.wangziyang.mes.system.dto.SysMenuDTO;
import com.wangziyang.mes.system.dto.SysRoleDTO;
import com.wangziyang.mes.system.dto.SysUserDTO;
import com.wangziyang.mes.system.entity.SysMenu;
import com.wangziyang.mes.system.mapper.SysMenuMapper;
import com.wangziyang.mes.system.service.ISysMenuService;
import com.wangziyang.mes.system.vo.TreeVO;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author SongPeng
 * @since 2019-10-16
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    /**
     * 根据角色id查询菜单列表
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public List<SysMenuDTO> listByRoleId(String roleId) throws Exception {
        return sysMenuMapper.listByRoleId(roleId);
    }

    /**
     * 获取当前登录用户有权限的菜单ID集合（包含父节点）
     */
    private Set<String> getCurrentUserMenuIds(List<SysMenu> allMenus) {
        Set<String> permittedMenuIds = new HashSet<>();
        SysUserDTO user = (SysUserDTO) SecurityUtils.getSubject().getPrincipal();
        if (user != null && user.getSysRoleDTOs() != null) {
            for (SysRoleDTO role : user.getSysRoleDTOs()) {
                if (role.getSysMenuDtos() != null) {
                    for (SysMenuDTO menu : role.getSysMenuDtos()) {
                        permittedMenuIds.add(menu.getId());
                    }
                }
            }
        }

        if (permittedMenuIds.isEmpty()) {
            return permittedMenuIds;
        }

        // 构建菜单映射，用于查找父节点
        Map<String, SysMenu> menuMap = new HashMap<>();
        for (SysMenu m : allMenus) {
            menuMap.put(m.getId(), m);
        }

        // 递归添加父节点ID
        Set<String> validIds = new HashSet<>(permittedMenuIds);
        for (String id : permittedMenuIds) {
            SysMenu current = menuMap.get(id);
            while (current != null && !"0".equals(current.getParentId())) {
                validIds.add(current.getParentId());
                current = menuMap.get(current.getParentId());
            }
        }
        return validIds;
    }

    /**
     * 系统首页初始化菜单树数据
     *
     * @return 系统首页初始化菜单树数据
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> listIndexMenuTree() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>(4);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.orderBy(true, true, "sort_num");
        List<SysMenu> allMenus = sysMenuMapper.selectList(queryWrapper);

        // 根据当前用户角色过滤菜单
        Set<String> validIds = getCurrentUserMenuIds(allMenus);
        List<SysMenu> sysMenus = new ArrayList<>();
        for (SysMenu m : allMenus) {
            if (validIds.contains(m.getId())) {
                sysMenus.add(m);
            }
        }

        Map<String, String> clearInfo = new HashMap<>(2);
        clearInfo.put("clearUrl", "json/clear.json");

        Map<String, String> homeInfo = new HashMap<>(4);
        homeInfo.put("name", "首页");
        homeInfo.put("icon", "fa fa-home");
        homeInfo.put("url", "admin/welcome-ui");

        Map<String, String> logoInfo = new HashMap<>(4);
        logoInfo.put("name", "黑科制造MES");
        logoInfo.put("image", "/image/logo.png");
        logoInfo.put("url", "");

        Map<String, Object> menuInfo = new LinkedHashMap<>(8);

        List<TreeVO<SysMenu>> menus = new ArrayList<>();
        for (SysMenu m : sysMenus) {
            TreeVO<SysMenu> tree = new TreeVO<>();
            tree.setId(m.getId());
            tree.setPid(m.getParentId());
            tree.setCode(m.getCode());
            tree.setName(m.getName());
            tree.setUrl(m.getUrl());
            tree.setIcon(m.getIcon());
            tree.setType(m.getType());
            tree.setPermission(m.getPermission());
            tree.setTarget("_self");
            menus.add(tree);
        }
        List<TreeVO<SysMenu>> treeVOS = TreeUtil.buildList(menus, "0");
        for (TreeVO<SysMenu> mTree : treeVOS) {
            menuInfo.put(mTree.getCode(), mTree);
        }

        result.put("clearInfo", clearInfo);
        result.put("homeInfo", homeInfo);
        result.put("logoInfo", logoInfo);
        result.put("menuInfo", menuInfo);

        return result;
    }

    /**
     * 用户搜索系统首页初始化菜单树数据
     * @param menuName 菜单名字
     * @return 菜单树数据
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> listIndexMenuSearchTree(String menuName) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>(4);
        Map<String, Object> menuInfo = new LinkedHashMap<>(8);
        List<SysMenu> sysMenus = sysMenuMapper.listBySearchByName(menuName);
        List<TreeVO<SysMenu>> menus = new ArrayList<>();
        for (SysMenu m : sysMenus) {
            TreeVO<SysMenu> tree = new TreeVO<>();
            tree.setId(m.getId());
            tree.setPid(m.getParentId());
            tree.setCode(m.getCode());
            tree.setName(m.getName());
            tree.setUrl(m.getUrl());
            tree.setIcon(m.getIcon());
            tree.setType(m.getType());
            tree.setPermission(m.getPermission());
            // TODO 是否需要更改？
            tree.setTarget("_self");
            menus.add(tree);
        }
        List<TreeVO<SysMenu>> treeVOS = TreeUtil.buildList(menus, "0");
        for (TreeVO<SysMenu> mTree : treeVOS) {
            menuInfo.put(mTree.getCode(), mTree);
        }
        result.put("menuInfo", menuInfo);
        return result;
    }

    /**
     * 获取系统菜单树
     *
     * @return 系统菜单树
     * @throws Exception 异常
     */
    @Override
    public List<TreeVO<SysMenu>> listMenuTree() throws Exception {
        List<TreeVO<SysMenu>> menus = new ArrayList<>();
        List<SysMenu> sysMenus = sysMenuMapper.selectList(null);
        for (SysMenu m : sysMenus) {
            TreeVO<SysMenu> tree = new TreeVO<>();
            tree.setId(m.getId());
            tree.setPid(m.getParentId());
            tree.setCode(m.getCode());
            tree.setName(m.getName());
            tree.setUrl(m.getUrl());
            tree.setIcon(m.getIcon());
            tree.setType(m.getType());
            tree.setPermission(m.getPermission());
            menus.add(tree);
        }
        return TreeUtil.buildList(menus, "0");
    }
}
