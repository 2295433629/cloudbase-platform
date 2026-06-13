package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysDept;
import com.cloudbase.module.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 部门管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService deptService;

    /**
     * 获取部门树
     */
    @Log(title = "部门管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/dept/tree")
    public AjaxResult tree() {
        return AjaxResult.success(deptService.getDeptTree());
    }

    /**
     * 新增部门
     */
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/dept/add")
    public AjaxResult add(@RequestBody SysDept dept) {
        if (dept.getParentId() != null && dept.getParentId() != 0) {
            SysDept parent = deptService.getById(dept.getParentId());
            if (parent != null) {
                dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
            }
        } else {
            dept.setAncestors("0");
        }
        deptService.save(dept);
        return AjaxResult.success();
    }

    /**
     * 编辑部门
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/dept/edit")
    public AjaxResult edit(@RequestBody SysDept dept) {
        deptService.updateById(dept);
        return AjaxResult.success();
    }

    /**
     * 删除部门
     */
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/dept/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        Long deptId = params.get("deptId");
        // 检查是否存在子部门
        long childCount = deptService.count(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId)
        );
        if (childCount > 0) {
            return AjaxResult.error("存在子部门，不允许删除");
        }
        deptService.removeById(deptId);
        return AjaxResult.success();
    }

    /**
     * 修改部门状态
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/dept/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        if (params.get("deptId") == null || params.get("status") == null) {
            return AjaxResult.error("参数缺失");
        }
        SysDept dept = new SysDept();
        dept.setDeptId(Long.parseLong(params.get("deptId").toString()));
        dept.setStatus(Integer.parseInt(params.get("status").toString()));
        deptService.updateById(dept);
        return AjaxResult.success();
    }
}