package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.model.dto.AssignRolesDTO;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.model.dto.UserCreateDTO;
import com.cloudbase.module.system.model.dto.UserQueryDTO;
import com.cloudbase.module.system.model.dto.UserUpdateDTO;
import com.cloudbase.module.system.service.ISysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理（重构后使用DTO+Service）
 */
@Validated
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;

    /**
     * 分页查询用户
     */
    @Log(title = "用户管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@Valid @RequestBody UserQueryDTO query) {
        var page = sysUserService.pageUsers(query);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 新增用户
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody UserCreateDTO dto) {
        SysUser user = new SysUser();
        user.setAccount(dto.getAccount());
        user.setPassword(dto.getPassword());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        sysUserService.createUser(user);
        return AjaxResult.success();
    }

    /**
     * 编辑用户
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody UserUpdateDTO dto) {
        SysUser user = new SysUser();
        user.setUserId(dto.getUserId());
        user.setPassword(dto.getPassword());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        sysUserService.updateUser(user);
        return AjaxResult.success();
    }

    /**
     * 删除用户
     */
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        sysUserService.deleteUser(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 修改用户状态
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody java.util.Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        sysUserService.updateStatus(userId, status);
        return AjaxResult.success();
    }

    /**
     * 查询用户已分配的角色ID列表
     */
    @Log(title = "用户管理", businessType = BusinessType.QUERY)
    @PostMapping("/roles")
    public AjaxResult getUserRoles(@Valid @RequestBody IdDTO dto) {
        return AjaxResult.success(sysUserService.getUserRoles(dto.getId()));
    }

    /**
     * 分配用户角色（全量替换）
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/assignRoles")
    public AjaxResult assignRoles(@Valid @RequestBody AssignRolesDTO dto) {
        sysUserService.assignRoles(dto.getUserId(), dto.getRoleIds());
        return AjaxResult.success();
    }

    /**
     * 重置用户密码
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody java.util.Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        String newPassword = params.get("newPassword").toString();
        if (newPassword == null || newPassword.length() < 6) {
            return AjaxResult.error("密码长度不能少于6位");
        }
        sysUserService.resetPassword(userId, newPassword);
        return AjaxResult.success();
    }
}
