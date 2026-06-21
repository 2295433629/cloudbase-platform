package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.model.dto.RoleQueryDTO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 创建角色
     */
    void createRole(SysRole role);

    /**
     * 更新角色
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 分页查询角色
     */
    Page<SysRole> pageRoles(RoleQueryDTO query);

    /**
     * 查询全部启用角色（下拉选择用）
     */
    List<SysRole> listEnabledRoles();

    /**
     * 修改角色状态
     */
    void updateStatus(Long roleId, Integer status);

    /**
     * 获取角色已分配的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 为角色分配菜单权限（全量替换）
     */
    void assignRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取用户所有权限标识（合并所有角色）
     * 超级管理员返回 ["*:*:*"]
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);
}
