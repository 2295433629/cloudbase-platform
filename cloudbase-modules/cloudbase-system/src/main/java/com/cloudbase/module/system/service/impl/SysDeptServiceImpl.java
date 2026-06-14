package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysDept;
import com.cloudbase.module.system.mapper.SysDeptMapper;
import com.cloudbase.module.system.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Slf4j
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Override
    public List<SysDept> getDeptTree() {
        List<SysDept> allDepts = list();
        // 找根节点(parentId=0或null)
        List<SysDept> roots = allDepts.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());
        roots.forEach(root -> buildChildren(root, allDepts));
        return roots;
    }

    @Override
    public void createDept(SysDept dept) {
        // 构建祖级关系
        if (dept.getParentId() != null && dept.getParentId() != 0) {
            SysDept parent = getById(dept.getParentId());
            if (parent != null) {
                dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
            }
        } else {
            dept.setAncestors("0");
        }
        save(dept);
    }

    @Override
    public void updateDept(SysDept dept) {
        updateById(dept);
    }

    @Override
    public void deleteDept(Long deptId) {
        // 检查是否存在子部门
        long childCount = count(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，不允许删除");
        }
        removeById(deptId);
    }

    @Override
    public void updateStatus(Long deptId, Integer status) {
        SysDept dept = new SysDept();
        dept.setDeptId(deptId);
        dept.setStatus(status);
        updateById(dept);
    }

    private void buildChildren(SysDept parent, List<SysDept> all) {
        List<SysDept> children = all.stream()
                .filter(d -> d.getParentId() != null && d.getParentId().equals(parent.getDeptId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildChildren(child, all));
    }
}
