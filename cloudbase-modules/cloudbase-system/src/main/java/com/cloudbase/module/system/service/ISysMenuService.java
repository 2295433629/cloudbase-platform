package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单树
     */
    List<SysMenu> getMenuTree();

    /**
     * 创建菜单
     */
    void createMenu(SysMenu menu);

    /**
     * 更新菜单
     */
    void updateMenu(SysMenu menu);

    /**
     * 删除菜单（检查子菜单）
     */
    void deleteMenu(Long menuId);

    /**
     * 修改菜单状态
     */
    void updateStatus(Long menuId, Integer status);
}
