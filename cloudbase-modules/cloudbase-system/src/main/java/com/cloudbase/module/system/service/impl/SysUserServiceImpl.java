package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.entity.SysUserRole;
import com.cloudbase.module.system.mapper.SysUserMapper;
import com.cloudbase.module.system.mapper.SysUserRoleMapper;
import com.cloudbase.module.system.model.dto.UserQueryDTO;
import com.cloudbase.module.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUser user) {
        if (user.getAccount() == null || user.getAccount().isEmpty()) {
            throw new BusinessException("账号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        // 校验账号唯一性
        long count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, user.getAccount()));
        if (count > 0) {
            throw new BusinessException("账号已存在");
        }
        // 密码加密
        String hashedPassword = LoginServiceImpl.sha256Hex(user.getPassword() + user.getAccount());
        user.setPassword(hashedPassword);
        save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        if (user.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        // 空密码不更新
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            SysUser existing = getById(user.getUserId());
            if (existing == null) {
                throw new BusinessException("用户不存在");
            }
            String hashedPassword = LoginServiceImpl.sha256Hex(user.getPassword() + existing.getAccount());
            user.setPassword(hashedPassword);
        } else {
            user.setPassword(null); // 不更新密码字段
        }
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        // 删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 删除用户
        removeById(userId);
    }

    @Override
    public Page<SysUser> pageUsers(UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getAccount() != null && !query.getAccount().isEmpty()) {
            wrapper.like(SysUser::getAccount, query.getAccount());
        }
        if (query.getRealName() != null && !query.getRealName().isEmpty()) {
            wrapper.like(SysUser::getRealName, query.getRealName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        int pageNo = Math.max(query.getPageNo() != null ? query.getPageNo() : 1, 1);
        int pageSize = Math.min(Math.max(query.getPageSize() != null ? query.getPageSize() : 20, 1), 200);

        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public List<Long> getUserRoles(Long userId) {
        return sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除旧角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 插入新角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        SysUser existing = getById(userId);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(LoginServiceImpl.sha256Hex(newPassword + existing.getAccount()));
        updateById(user);
    }
}
