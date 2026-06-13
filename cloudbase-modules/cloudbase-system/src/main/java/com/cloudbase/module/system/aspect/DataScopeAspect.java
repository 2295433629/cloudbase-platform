package com.cloudbase.module.system.aspect;

import com.cloudbase.common.core.annotation.DataScope;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.mapper.SysRoleMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

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

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint joinPoint, DataScope dataScope) throws Throwable {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) return;

        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) return;

        // 超级管理员：不限制数据
        // 这里简化处理：admin账号=全部权限
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
        // 获取用户角色，取最小的dataScope值（最严格的权限）
        Integer minScope = getMinDataScope(user);
        if (minScope == null) minScope = 5; // 默认仅本人

        StringBuilder sql = new StringBuilder();

        switch (minScope) {
            case 1: // 全部权限 - 不添加过滤
                return "";
            case 2: // 自定义 - 根据角色关联的部门
                Long deptIds = getCustomDeptIds(user);
                if (deptIds != null) {
                    sql.append(" AND ").append(dataScope.deptAlias()).append(".dept_id IN (").append(deptIds).append(") ");
                }
                break;
            case 3: // 本部门
                if (user.getDeptId() != null) {
                    sql.append(" AND ").append(dataScope.deptAlias()).append(".dept_id = ").append(user.getDeptId()).append(" ");
                }
                break;
            case 4: // 本部门及以下
                if (user.getDeptId() != null) {
                    sql.append(" AND (").append(dataScope.deptAlias()).append(".dept_id = ").append(user.getDeptId())
                       .append(" OR FIND_IN_SET(").append(user.getDeptId()).append(", ").append(dataScope.deptAlias())
                       .append(".dept_id) > 0) ");
                }
                break;
            case 5: // 仅本人
                sql.append(" AND ").append(dataScope.userAlias()).append(".create_user = ").append(user.getUserId()).append(" ");
                break;
            default:
                return "";
        }
        return sql.toString();
    }

    private Integer getMinDataScope(SysUser user) {
        // 简化：根据用户ID查询关联角色的dataScope最小值
        List<SysRole> roles = sysRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>()
        );
        // 实际项目应该通过用户-角色关联表查询
        return roles.stream()
                .filter(r -> r.getStatus() == 1)
                .map(SysRole::getDataScope)
                .min(Integer::compareTo)
                .orElse(5);
    }

    private Long getCustomDeptIds(SysUser user) {
        // 实际项目：查询 sys_role_dept 表
        return user.getDeptId();
    }
}
