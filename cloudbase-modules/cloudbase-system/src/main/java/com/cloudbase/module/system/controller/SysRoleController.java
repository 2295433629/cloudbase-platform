package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.model.dto.RoleCreateDTO;
import com.cloudbase.module.system.model.dto.RoleQueryDTO;
import com.cloudbase.module.system.model.dto.RoleUpdateDTO;
import com.cloudbase.module.system.service.ISysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理（重构后使用DTO+Service）
 */
@Validated
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService sysRoleService;

    /**
     * 分页查询角色
     */
    @Log(title = "角色管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@Valid @RequestBody RoleQueryDTO query) {
        var page = sysRoleService.pageRoles(query);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询全部角色（不分页，用于下拉选择）
     */
    @Log(title = "角色管理", businessType = BusinessType.QUERY)
    @PostMapping("/list")
    public AjaxResult list() {
        return AjaxResult.success(sysRoleService.listEnabledRoles());
    }

    /**
     * 新增角色
     */
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody RoleCreateDTO dto) {
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setSort(dto.getSort() != null ? dto.getSort() : 0);
        role.setDataScope(dto.getDataScope());
        role.setStatus(dto.getStatus());
        role.setRemark(dto.getRemark());
        sysRoleService.createRole(role);
        return AjaxResult.success();
    }

    /**
     * 编辑角色
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody RoleUpdateDTO dto) {
        SysRole role = new SysRole();
        role.setRoleId(dto.getRoleId());
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        if (dto.getSort() != null) {
            role.setSort(dto.getSort());
        }
        role.setDataScope(dto.getDataScope());
        role.setStatus(dto.getStatus());
        role.setRemark(dto.getRemark());
        sysRoleService.updateRole(role);
        return AjaxResult.success();
    }

    /**
     * 删除角色
     */
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        sysRoleService.deleteRole(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 修改角色状态
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.get("roleId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        sysRoleService.updateStatus(roleId, status);
        return AjaxResult.success();
    }

    /**
     * 获取角色已分配的菜单ID列表
     */
    @PostMapping("/menus")
    public AjaxResult getMenus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.get("roleId").toString());
        return AjaxResult.success(sysRoleService.getRoleMenuIds(roleId));
    }

    /**
     * 为角色分配菜单权限（全量替换）
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/assignMenus")
    public AjaxResult assignMenus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> menuIds = ((List<Object>) params.get("menuIds")).stream()
                .map(o -> Long.parseLong(o.toString()))
                .collect(Collectors.toList());
        sysRoleService.assignRoleMenus(roleId, menuIds);
        return AjaxResult.success("分配成功");
    }
}
