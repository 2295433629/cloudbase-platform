package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 角色管理
 */
@RestController
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService sysRoleService;

    /**
     * 分页查询角色
     */
    @Log(title = "角色管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/role/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("roleName") && params.get("roleName") != null
                && !params.get("roleName").toString().isEmpty()) {
            wrapper.like(SysRole::getRoleName, params.get("roleName"));
        }
        if (params.containsKey("roleCode") && params.get("roleCode") != null
                && !params.get("roleCode").toString().isEmpty()) {
            wrapper.like(SysRole::getRoleCode, params.get("roleCode"));
        }
        if (params.containsKey("status") && params.get("status") != null) {
            wrapper.eq(SysRole::getStatus, params.get("status"));
        }
        wrapper.orderByAsc(SysRole::getSort);

        Page<SysRole> page = sysRoleService.page(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询全部角色（不分页，用于下拉选择）
     */
    @Log(title = "角色管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/role/list")
    public AjaxResult list() {
        List<SysRole> roles = sysRoleService.list(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSort)
        );
        return AjaxResult.success(roles);
    }

    /**
     * 新增角色
     */
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/role/add")
    public AjaxResult add(@RequestBody SysRole role) {
        // 校验角色编码唯一
        long count = sysRoleService.count(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode())
        );
        if (count > 0) {
            return AjaxResult.error("角色编码已存在");
        }
        sysRoleService.save(role);
        return AjaxResult.success();
    }

    /**
     * 编辑角色
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/role/edit")
    public AjaxResult edit(@RequestBody SysRole role) {
        // 校验角色编码唯一（排除自身）
        long count = sysRoleService.count(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, role.getRoleCode())
                        .ne(SysRole::getRoleId, role.getRoleId())
        );
        if (count > 0) {
            return AjaxResult.error("角色编码已存在");
        }
        sysRoleService.updateById(role);
        return AjaxResult.success();
    }

    /**
     * 删除角色
     */
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/role/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        sysRoleService.removeById(params.get("roleId"));
        return AjaxResult.success();
    }

    /**
     * 修改角色状态
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/role/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        if (params.get("roleId") == null || params.get("status") == null) {
            return AjaxResult.error("参数缺失");
        }
        SysRole role = new SysRole();
        role.setRoleId(Long.parseLong(params.get("roleId").toString()));
        role.setStatus(Integer.parseInt(params.get("status").toString()));
        sysRoleService.updateById(role);
        return AjaxResult.success();
    }
}
