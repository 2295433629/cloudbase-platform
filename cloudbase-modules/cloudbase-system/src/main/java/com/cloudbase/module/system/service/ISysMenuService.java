package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysMenu;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    /** 获取菜单树 */
    List<SysMenu> getMenuTree();
}
