package com.cloudbase.module.system.aspect;

import com.cloudbase.common.core.annotation.DataScope;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.entity.SysUserRole;
import com.cloudbase.module.system.mapper.SysRoleMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import com.cloudbase.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限切面 - 核心设计：在SQL执行前注入数据过滤条件
 *
 * 五种数据权限：
 *   1 = 全部数据权限（不添加任何过滤）
 *   2 = 自定义数据权限（根据角色关联的部门ID过滤）
 *   3 = 本部门数据权限（仅查本人所在部门）
 *   4 = 本部门及以下（用find_in_set查所有子部门）
 *   5 = 仅本人数据（只查 create_user = 当前用户ID）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint joinPoint, DataScope dataScope) throws Throwable {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) return;

        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) return;

        // 超级管理员：不限制数据
        if ("admin".equals(currentUser.getAccount())) return;

        // 查找可注入dataScope的Map参数
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) arg;
                map.put("dataScope", buildDataScopeSql(currentUser, dataScope));
                return;
            }
        }
    }

    private String buildDataScopeSql(SysUser user, DataScope dataScope) {
        Integer minScope = getMinDataScope(user);
        if (minScope == null) minScope = 5;

        StringBuilder sql = new StringBuilder();

        switch (minScope) {
            case 1:
                return "";
            case 2:
                Long deptIds = getCustomDeptIds(user);
                if (deptIds != null) {
                    sql.append(" AND ").append(dataScope.deptAlias()).append(".dept_id IN (").append(deptIds).append(") ");
                }
                break;
            case 3:
                if (user.getDeptId() != null) {
                    sql.append(" AND ").append(dataScope.deptAlias()).append(".dept_id = ").append(user.getDeptId()).append(" ");
                }
                break;
            case 4:
                if (user.getDeptId() != null) {
                    sql.append(" AND (").append(dataScope.deptAlias()).append(".dept_id = ").append(user.getDeptId())
                       .append(" OR FIND_IN_SET(").append(user.getDeptId()).append(", ").append(dataScope.deptAlias())
                       .append(".dept_id) > 0) ");
                }
                break;
            case 5:
                sql.append(" AND ").append(dataScope.userAlias()).append(".create_user = ").append(user.getUserId()).append(" ");
                break;
            default:
                return "";
        }
        return sql.toString();
    }

    /**
     * 通过用户-角色关联表查询当前用户的角色，取最小dataScope
     * FIX: 原实现查询所有角色，现已修正为通过SysUserRoleMapper关联查询
     */
    private Integer getMinDataScope(SysUser user) {
        // 通过用户-角色关联表查询当前用户的所有角色ID
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getUserId())
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return 5; // 无角色：仅本人
        }

        // 查询这些角色的详细信息，取启用的角色中最小dataScope
        List<SysRole> roles = sysRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleId, roleIds)
                        .eq(SysRole::getStatus, 1)
        );

        return roles.stream()
                .map(SysRole::getDataScope)
                .min(Integer::compareTo)
                .orElse(5);
    }

    private Long getCustomDeptIds(SysUser user) {
        // 实际项目：查询 sys_role_dept 表
        return user.getDeptId();
    }
}
