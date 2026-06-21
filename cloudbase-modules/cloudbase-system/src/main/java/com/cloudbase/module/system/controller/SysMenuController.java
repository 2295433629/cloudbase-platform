package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.entity.SysMenu;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.model.dto.MenuCreateDTO;
import com.cloudbase.module.system.model.dto.MenuUpdateDTO;
import com.cloudbase.module.system.service.ISysMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单管理（重构后使用DTO+Service）
 */
@Validated
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService sysMenuService;

    /**
     * 获取菜单树（全量，不分页，菜单管理页使用）
     */
    @Log(title = "菜单管理", businessType = BusinessType.QUERY)
    @PostMapping("/tree")
    public AjaxResult tree() {
        return AjaxResult.success(sysMenuService.getMenuTree());
    }

    /**
     * 获取当前用户有权限的菜单树（侧边栏/动态路由用）
     */
    @PostMapping("/userTree")
    public AjaxResult userTree() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        return AjaxResult.success(sysMenuService.getMenuTreeByUserId(userId));
    }

    /**
     * 新增菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody MenuCreateDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPerms(dto.getPerms());
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort());
        menu.setHidden(dto.getHidden() != null ? dto.getHidden() : 0);
        menu.setStatus(dto.getStatus());
        sysMenuService.createMenu(menu);
        return AjaxResult.success();
    }

    /**
     * 编辑菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody MenuUpdateDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(dto.getMenuId());
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPerms(dto.getPerms());
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort());
        menu.setHidden(dto.getHidden() != null ? dto.getHidden() : 0);
        menu.setStatus(dto.getStatus());
        sysMenuService.updateMenu(menu);
        return AjaxResult.success();
    }

    /**
     * 删除菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        sysMenuService.deleteMenu(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 修改菜单状态
     */
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody java.util.Map<String, Object> params) {
        Long menuId = Long.parseLong(params.get("menuId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        sysMenuService.updateStatus(menuId, status);
        return AjaxResult.success();
    }
}
