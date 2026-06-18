package com.wangziyang.mes.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangziyang.mes.system.entity.SysRoleMenu;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-05
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

	/**
	 * 根据角色ID删除角色菜单关联
	 *
	 * @param roleId 角色ID
	 * @return 影响行数
	 */
	int deleteByRoleId(String roleId);

	/**
	 * 批量插入角色菜单关联
	 *
	 * @param list 角色菜单关联列表
	 * @return 影响行数
	 */
	int batchInsert(List<SysRoleMenu> list);
}
