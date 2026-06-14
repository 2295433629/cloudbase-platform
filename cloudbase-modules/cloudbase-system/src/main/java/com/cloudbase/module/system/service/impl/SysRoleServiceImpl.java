package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.mapper.SysRoleMapper;
import com.cloudbase.module.system.model.dto.RoleQueryDTO;
import com.cloudbase.module.system.service.ISysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public void createRole(SysRole role) {
        // 校验角色编码唯一
        long count = count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        save(role);
    }

    @Override
    public void updateRole(SysRole role) {
        // 校验角色编码唯一（排除自身）
        long count = count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, role.getRoleCode())
                .ne(SysRole::getRoleId, role.getRoleId()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        updateById(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        removeById(roleId);
    }

    @Override
    public Page<SysRole> pageRoles(RoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (query.getRoleName() != null && !query.getRoleName().isEmpty()) {
            wrapper.like(SysRole::getRoleName, query.getRoleName());
        }
        if (query.getRoleCode() != null && !query.getRoleCode().isEmpty()) {
            wrapper.like(SysRole::getRoleCode, query.getRoleCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysRole::getSort);

        int pageNo = Math.max(query.getPageNo() != null ? query.getPageNo() : 1, 1);
        int pageSize = Math.min(Math.max(query.getPageSize() != null ? query.getPageSize() : 20, 1), 200);

        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    @Override
    public List<SysRole> listEnabledRoles() {
        return list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort));
    }

    @Override
    public void updateStatus(Long roleId, Integer status) {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setStatus(status);
        updateById(role);
    }
}
