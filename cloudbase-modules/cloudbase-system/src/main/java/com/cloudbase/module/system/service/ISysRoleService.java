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
}
