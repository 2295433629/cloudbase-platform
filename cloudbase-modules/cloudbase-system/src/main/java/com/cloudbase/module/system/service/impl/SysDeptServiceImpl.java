package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.module.system.entity.SysDept;
import com.cloudbase.module.system.mapper.SysDeptMapper;
import com.cloudbase.module.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Override
    public List<SysDept> getDeptTree() {
        List<SysDept> allDepts = list();
        // 找根节点(parentId=0或null)
        List<SysDept> roots = allDepts.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());
        // 递归构建树
        roots.forEach(root -> buildChildren(root, allDepts));
        return roots;
    }

    private void buildChildren(SysDept parent, List<SysDept> all) {
        List<SysDept> children = all.stream()
                .filter(d -> d.getParentId() != null && d.getParentId().equals(parent.getDeptId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildChildren(child, all));
    }
}
