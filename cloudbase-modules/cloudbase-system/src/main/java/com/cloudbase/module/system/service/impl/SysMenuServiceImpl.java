package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysMenu;
import com.cloudbase.module.system.mapper.SysMenuMapper;
import com.cloudbase.module.system.service.ISysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> all = list(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort)
        );
        // 找根节点（parentId=0或null）
        List<SysMenu> roots = all.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        roots.forEach(root -> buildChildren(root, all));
        return roots;
    }

    @Override
    public void createMenu(SysMenu menu) {
        save(menu);
    }

    @Override
    public void updateMenu(SysMenu menu) {
        updateById(menu);
    }

    @Override
    public void deleteMenu(Long menuId) {
        // 检查是否存在子菜单
        long childCount = count(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，不允许删除");
        }
        removeById(menuId);
    }

    @Override
    public void updateStatus(Long menuId, Integer status) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(menuId);
        menu.setStatus(status);
        updateById(menu);
    }

    private void buildChildren(SysMenu parent, List<SysMenu> all) {
        List<SysMenu> children = all.stream()
                .filter(m -> m.getParentId() != null && m.getParentId().equals(parent.getMenuId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildChildren(child, all));
    }
}
