package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysMenu;
import com.cloudbase.module.system.mapper.SysMenuMapper;
import com.cloudbase.module.system.mapper.SysRoleMenuMapper;
import com.cloudbase.module.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        return buildTree(listAll());
    }

    @Override
    public List<SysMenu> getMenuTreeByUserId(Long userId) {
        // 超级管理员返回全量菜单
        if (userId != null && userId == 1L) {
            return buildTree(listAll());
        }
        // 查询用户有权限的菜单ID
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByUserId(userId);
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        Set<Long> menuIdSet = new HashSet<>(menuIds);
        // 查出这些菜单及其所有祖先（确保树结构完整）
        List<SysMenu> all = listAll();
        Set<Long> includedIds = new HashSet<>(menuIdSet);
        // 向上补全父节点
        for (Long mid : menuIdSet) {
            addAncestors(mid, all, includedIds);
        }
        List<SysMenu> filtered = all.stream()
                .filter(m -> includedIds.contains(m.getMenuId()))
                .collect(Collectors.toList());
        return buildTree(filtered);
    }

    private List<SysMenu> listAll() {
        return list(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
    }

    private List<SysMenu> buildTree(List<SysMenu> all) {
        List<SysMenu> roots = all.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        roots.forEach(root -> buildChildren(root, all));
        return roots;
    }

    private void addAncestors(Long menuId, List<SysMenu> all, Set<Long> includedIds) {
        all.stream()
                .filter(m -> m.getMenuId().equals(menuId))
                .findFirst()
                .ifPresent(m -> {
                    Long parentId = m.getParentId();
                    if (parentId != null && parentId != 0 && !includedIds.contains(parentId)) {
                        includedIds.add(parentId);
                        addAncestors(parentId, all, includedIds);
                    }
                });
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
