package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.entity.SysUserRole;
import com.cloudbase.module.system.mapper.SysUserRoleMapper;
import com.cloudbase.module.system.service.AuthService;
import com.cloudbase.module.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 分页查询用户
     */
    @Log(title = "用户管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/user/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("account")) {
            wrapper.like(SysUser::getAccount, params.get("account"));
        }
        if (params.containsKey("realName")) {
            wrapper.like(SysUser::getRealName, params.get("realName"));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = sysUserService.page(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 新增用户
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/user/add")
    public AjaxResult add(@RequestBody SysUser user) {
        if (user.getAccount() == null || user.getAccount().isEmpty()) {
            return AjaxResult.error("账号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return AjaxResult.error("密码不能为空");
        }
        // 检查账号唯一性
        long count = sysUserService.count(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, user.getAccount())
        );
        if (count > 0) {
            return AjaxResult.error("账号已存在");
        }
        // 密码加密：SHA-256(password + account作为盐)
        String hashedPassword = AuthService.sha256Hex(user.getPassword() + user.getAccount());
        user.setPassword(hashedPassword);
        sysUserService.save(user);
        return AjaxResult.success();
    }

    /**
     * 编辑用户
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/user/edit")
    public AjaxResult edit(@RequestBody SysUser user) {
        if (user.getUserId() == null) {
            return AjaxResult.error("用户ID不能为空");
        }
        // 密码处理：空密码表示不修改密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 需要获取账号名来做盐值
            SysUser existing = sysUserService.getById(user.getUserId());
            if (existing == null) {
                return AjaxResult.error("用户不存在");
            }
            String hashedPassword = AuthService.sha256Hex(user.getPassword() + existing.getAccount());
            user.setPassword(hashedPassword);
        } else {
            user.setPassword(null); // 不更新密码字段
        }
        sysUserService.updateById(user);
        return AjaxResult.success();
    }

    /**
     * 删除用户
     */
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/user/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        sysUserService.removeById(params.get("userId"));
        return AjaxResult.success();
    }

    /**
     * 修改用户状态
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/user/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        if (params.get("userId") == null || params.get("status") == null) {
            return AjaxResult.error("参数缺失");
        }
        SysUser user = new SysUser();
        user.setUserId(Long.parseLong(params.get("userId").toString()));
        user.setStatus(Integer.parseInt(params.get("status").toString()));
        sysUserService.updateById(user);
        return AjaxResult.success();
    }

    /**
     * 查询用户已分配的角色ID列表
     */
    @Log(title = "用户管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/user/roles")
    public AjaxResult getUserRoles(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        if (userId == null) {
            return AjaxResult.error("参数缺失: userId");
        }
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return AjaxResult.success(roleIds);
    }

    /**
     * 分配用户角色（全量替换）
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @SuppressWarnings("unchecked")
    @PostMapping("/sys/user/assignRoles")
    public AjaxResult assignRoles(@RequestBody Map<String, Object> params) {
        if (params.get("userId") == null) {
            return AjaxResult.error("参数缺失: userId");
        }
        Long userId = Long.parseLong(params.get("userId").toString());
        List<Number> roleIds = (List<Number>) params.get("roleIds");

        // 先删除该用户所有旧角色
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        // 插入新角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Number roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId.longValue());
                sysUserRoleMapper.insert(ur);
            }
        }
        return AjaxResult.success();
    }
}