package com.cloudbase.module.system.aspect;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.common.core.annotation.DataScope;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.entity.SysRoleDept;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.entity.SysUserRole;
import com.cloudbase.module.system.mapper.SysRoleDeptMapper;
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
 * 数据权限切面 - 在 Service 方法执行前构建数据过滤条件
 *
 * 五种数据权限：
 *   1 = 全部数据权限（不添加任何过滤）
 *   2 = 自定义数据权限（根据角色关联的部门ID过滤 sys_role_dept）
 *   3 = 本部门数据权限（仅查本人所在部门）
 *   4 = 本部门及以下（FIND_IN_SET 查 ancestors 字段）
 *   5 = 仅本人数据（只查 create_user = 当前用户ID）
 *
 * 桥接机制：
 *   - 切面将条件存入 DataScopeHelper (ThreadLocal)
 *   - Service 方法构建 wrapper 后调用 DataScopeHelper.applyTo(wrapper)
 *   - 若方法参数中已有 AbstractWrapper，切面直接注入
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint joinPoint, DataScope dataScope) throws Throwable {
        // 每次进入前清理上一次残留（防止内存泄漏）
        DataScopeHelper.clear();

        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) return;

        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) return;

        // 超级管理员：不限制数据
        if ("admin".equals(currentUser.getAccount())) return;

        Integer minScope = getMinDataScope(currentUser);
        if (minScope == null || minScope == 1) return; // 全部权限，不过滤

        // 构建 SQL 条件
        String sql = buildSql(currentUser, dataScope, minScope);
        if (sql == null || sql.isEmpty()) return;

        // 存入 ThreadLocal，供 Service 方法消费
        DataScopeHelper.setCondition(sql);

        // 兼容：若方法参数中有 AbstractWrapper，直接注入
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof AbstractWrapper) {
                @SuppressWarnings("unchecked")
                AbstractWrapper<?, ?, ?> wrapper = (AbstractWrapper<?, ?, ?>) arg;
                DataScopeHelper.applyTo(wrapper);
                return;
            }
        }
    }

    /**
     * 根据用户权限等级构建 SQL 过滤条件（不含前导 AND）
     */
    private String buildSql(SysUser user, DataScope dataScope, Integer minScope) {
        String deptCol = prefix(dataScope.deptAlias(), "dept_id");
        String userCol = prefix(dataScope.userAlias(), "create_user");
        String ancestorsCol = prefix(dataScope.deptAlias(), dataScope.ancestors());

        switch (minScope) {
            case 2: { // 自定义数据权限
                List<Long> deptIds = getCustomDeptIds(user);
                if (deptIds.isEmpty()) return null;
                String ids = deptIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                return deptCol + " IN (" + ids + ")";
            }
            case 3: // 本部门数据权限
                return user.getDeptId() != null
                        ? deptCol + " = " + user.getDeptId()
                        : null;
            case 4: // 本部门及以下
                return user.getDeptId() != null
                        ? "(" + deptCol + " = " + user.getDeptId()
                          + " OR FIND_IN_SET(" + user.getDeptId() + ", " + ancestorsCol + ") > 0)"
                        : null;
            case 5: // 仅本人
                return userCol + " = " + user.getUserId();
            default:
                return null;
        }
    }

    /**
     * 拼接 别名.字段名；若无别名则直接返回字段名
     */
    private String prefix(String alias, String column) {
        return (alias == null || alias.isEmpty()) ? column : alias + "." + column;
    }

    /**
     * 通过用户-角色关联表查询当前用户的角色，取最小 dataScope（最大权限）
     */
    private Integer getMinDataScope(SysUser user) {
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getUserId())
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return 5;
        }

        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleId, roleIds)
                        .eq(SysRole::getStatus, 1)
        );

        return roles.stream()
                .map(SysRole::getDataScope)
                .filter(ds -> ds != null)
                .min(Integer::compareTo)
                .orElse(5);
    }

    /**
     * 查询自定义数据权限关联的部门ID列表（从 sys_role_dept 表）
     */
    private List<Long> getCustomDeptIds(SysUser user) {
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getUserId())
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return sysRoleDeptMapper.selectList(
                new LambdaQueryWrapper<SysRoleDept>()
                        .in(SysRoleDept::getRoleId, roleIds)
        ).stream().map(SysRoleDept::getDeptId).distinct().collect(Collectors.toList());
    }
}
