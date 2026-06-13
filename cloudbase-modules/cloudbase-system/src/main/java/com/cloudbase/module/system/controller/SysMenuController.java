package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysMenu;
import com.cloudbase.module.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 菜单管理
 */
@RestController
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService sysMenuService;

    /**
     * 获取菜单树（全量，不分页）
     */
    @Log(title = "菜单管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/menu/tree")
    public AjaxResult tree() {
        return AjaxResult.success(sysMenuService.getMenuTree());
    }

    /**
     * 新增菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/menu/add")
    public AjaxResult add(@RequestBody SysMenu menu) {
        sysMenuService.save(menu);
        return AjaxResult.success();
    }

    /**
     * 编辑菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/menu/edit")
    public AjaxResult edit(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return AjaxResult.success();
    }

    /**
     * 删除菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/menu/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        Long menuId = params.get("menuId");
        // 检查是否存在子菜单
        long childCount = sysMenuService.count(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId)
        );
        if (childCount > 0) {
            return AjaxResult.error("存在子菜单，不允许删除");
        }
        sysMenuService.removeById(menuId);
        return AjaxResult.success();
    }

    /**
     * 修改菜单状态
     */
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/menu/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        if (params.get("menuId") == null || params.get("status") == null) {
            return AjaxResult.error("参数缺失");
        }
        SysMenu menu = new SysMenu();
        menu.setMenuId(Long.parseLong(params.get("menuId").toString()));
        menu.setStatus(Integer.parseInt(params.get("status").toString()));
        sysMenuService.updateById(menu);
        return AjaxResult.success();
    }
}
