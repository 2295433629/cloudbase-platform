package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.model.dto.UserQueryDTO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 创建用户（校验账号唯一性、加密密码）
     */
    void createUser(SysUser user);

    /**
     * 更新用户（空密码不更新）
     */
    void updateUser(SysUser user);

    /**
     * 删除用户（同时删除用户角色关联）
     */
    void deleteUser(Long userId);

    /**
     * 分页查询用户
     */
    Page<SysUser> pageUsers(UserQueryDTO query);

    /**
     * 修改用户状态
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 查询用户已分配的角色ID列表
     */
    List<Long> getUserRoles(Long userId);

    /**
     * 分配用户角色（全量替换）
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);
}
