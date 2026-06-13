package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.module.system.entity.SysMenu;
import com.cloudbase.module.system.mapper.SysMenuMapper;
import com.cloudbase.module.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> all = list(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort)
        );
        // 找根节点（parentId=0）
        List<SysMenu> roots = all.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        roots.forEach(root -> buildChildren(root, all));
        return roots;
    }

    private void buildChildren(SysMenu parent, List<SysMenu> all) {
        List<SysMenu> children = all.stream()
                .filter(m -> m.getParentId() != null && m.getParentId().equals(parent.getMenuId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildChildren(child, all));
    }
}
